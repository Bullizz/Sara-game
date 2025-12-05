package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import entities.Enemy;
import entities.Player;
import mini_games.Albin;
import mini_games.Attila;
import mini_games.Lkab;
import mini_games.Lulle;
import mini_games.Pauline;
import mini_games.SSC;
import mini_games.Slusk;

public class GamePanel extends JPanel
{
	KeyHandler key_handler;
	Player player;
	Enemy lulle, albin, lkab, ssc, slusk, attila, pauline;
	final int enemy_amount  = 7;
	
	Enemy[] enemies;
	GameTimer game_timer;
	
	Thread game_thread, enemies_thread;
	
	final int width;
	final int height;
	
	boolean game_loop_running, game_paused, enemies_updating;
	double random_speed_coeff;
	
	JFrame frame;
	JLabel top;
	
	BufferedImage map_img;
	boolean map_drawn = false;
	
	int[][] map_constraints;
	
	int enemy_collision_index = -1;
	double delta = 0;
	
	MouseList ml;
	
	public GamePanel(JFrame frame, JLabel top, GameTimer game_timer, KeyHandler key_handler, int player_x0, int player_y0)
	{
		super();
			this.width = frame.getWidth();
			this.height = 9 * (frame.getHeight() / 10);
		setPreferredSize(new Dimension(this.width, this.height));
		setLocation(0, 0);
		setFocusable(false);
		
		try
		{
			map_img = ImageIO.read(getClass().getResourceAsStream("/image_files/europe_4.png"));
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		
		frame.add(this);
		if(!frame.isVisible())
			frame.setVisible(true);

		int entity_width  = this.width / 40;
		int entity_height = this.height / 40;
		
		// Init. player
		player	= new Player(972, 101, entity_height, entity_width);
		
		// Init. enemies
		lulle 	= new Enemy("lulle",	0,  1460,  259,  entity_height, entity_width);
		albin	= new Enemy("albin",	3,  1468,  356,  entity_height, entity_width);
		lkab	= new Enemy("lkab",		1,  664,   313,  entity_height, entity_width);
		ssc		= new Enemy("ssc",		2,  1665,  107,  entity_height, entity_width);
		slusk	= new Enemy("slusk",	1,  1402,  567,  entity_height, entity_width);
		attila	= new Enemy("attila",	0,  807,   739,  entity_height, entity_width);
		pauline = new Enemy("pauline",	2,  504,   647,  entity_height, entity_width);
		
		enemies = new Enemy[]{lulle, albin, lkab, ssc, slusk, attila, pauline};
		
		this.frame 		 = frame;
		this.top 		 = top;
		this.key_handler = key_handler;
		this.game_timer  = game_timer;
		
		// Integer matrix with map boundaries
		map_constraints = loadMapConstraints(this.width, this.height);
		
//		ml = new MouseList(this.width, this.height, frame.getHeight() / 10);
//		frame.addMouseMotionListener(ml);
		
//		addMouseListener(new MouseAdapter() {
//			public void mousePressed(MouseEvent e)
//			{
//				JOptionPane.showMessageDialog(null, e.getX() + ", " + e.getY());
////				System.out.println(e.getX() + ", " + e.getY());
////				System.exit(0);
//			}
//		});
		
		initGameThread();
		initEnemiesThread();
	}

	int FPS = 60;
	private void initGameThread()
	{
		if(game_timer == null)
		{
			game_timer = new GameTimer();
			game_timer.initTimer(top);
		}
		
		game_loop_running = true;
		game_thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// Master game-loop
				while(game_thread != null)
				{
					/*
					 * Time-to-update logic
					 * Src: https://www.youtube.com/watch?v=VpH33Uw-_0E&t=1906s
					 */
					double draw_interval = Math.pow(10, 9);
					draw_interval /= FPS;
					double delta = 0;
					long last_time = System.nanoTime();
					long current_time;
					
					boolean game_paused = key_handler.isGame_paused();
					
					// Assign timer first value to start it
					// (only when master loop is started)
					if(!game_paused && game_loop_running)
						game_timer.setTime_coeff(1);
					
					// Slave game-loop
					while(game_loop_running && !game_paused)
					{
						// Cont. time-to-update logic
						current_time = System.nanoTime();
						delta += (current_time - last_time) / draw_interval;
						last_time = current_time;
						if(delta >= 1)
						{
							updatePlayer();
							
							repaint();
							
							// Player-enemy collision
							int[] collision_params = checkCollision( player.getPlayer_x(), player.getPlayer_y(), player.player_width, player.player_height, -1);
							
							// Collision
							if(collision_params[0] == 1)
							{
								enemy_collision_index = collision_params[1];
								break;
							}
							
							delta = 0;
						}
						
					} // End of slave game-loop
					
					// Collision detected
					if(enemy_collision_index > -1)
					{
						game_loop_running = false;
						game_thread		  = null;
						
						enemies_updating  = false;
						enemies_thread	  = null;
						
						launchMiniGame(enemy_collision_index);
					}
				} // End of master game-loop
			}
		});
		game_thread.start();
	}
	
	private void initEnemiesThread()
	{
		enemies_updating = true;
		enemies_thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				// Master game-loop
				while(enemies_thread != null)
				{
					double draw_interval = Math.pow(10, 9);
					draw_interval /= FPS;
					double delta = 0;
					long last_time = System.nanoTime();
					long current_time;
					
					// Slave game-loop
					while(enemies_updating && !game_paused)
					{
						current_time = System.nanoTime();
						delta += (current_time - last_time) / draw_interval;
						last_time = current_time;
						if(delta >= 1)
						{
							updateEnemies();
							
							delta = 0;
						}
					} // End of slave game-loop
				} // End of master game-loop
			}
		});
		enemies_thread.start();
	}
	
	private void updatePlayer()
	{
		// Get key pressed
		int[] player_direction_arr = key_handler.getDirection_arr();
		
		// Get positive/negative direction of player
		int player_dx = player_direction_arr[0];
		int player_dy = player_direction_arr[1];
		
		// Get current speed of player
		int player_speed_x = player.getPlayer_speed_x();
		int player_speed_y = player.getPlayer_speed_y();
		
		// Horizontal acceleration
		if((key_handler.LEFT || key_handler.RIGHT) && player_speed_x < player.max_speed)
			player_speed_x++;
		else if((!key_handler.LEFT || !key_handler.RIGHT) && player_speed_x > 0)
			player_speed_x--;

		// Vertical acceleration
		if((key_handler.UP || key_handler.DOWN) && player_speed_y < player.max_speed)
			player_speed_y++;
		else if((!key_handler.UP || !key_handler.DOWN) && player_speed_y > 0)
			player_speed_y--;
		
		// Get position of player
		int player_x = player.getPlayer_x();
		int player_y = player.getPlayer_y();
		
		// If within map constraints
		if(moveableX2(player_x, player_y, player.player_width, player.player_height, player_dx))
			player_x += player_speed_x * player_dx;
		if(moveableY2(player_x, player_y, player.player_width, player.player_height, player_dy))
			player_y += player_speed_y * player_dy;
		
		player.setPlayer_x(player_x);
		player.setPlayer_y(player_y);
		player.setPlayer_speed_x(player_speed_x);
		player.setPlayer_speed_y(player_speed_y);
	}

	private void updateEnemies()
	{
		/*
		 *  ____________________________________
		 * | follow-type |     description      |
		 * |____________________________________|
		 * |       0     | Follow at full speed |
		 * |____________________________________|
		 * |       1     | Follow at 50% speed  |
		 * |____________________________________|
		 * |       2     | Randomized direction |
		 * |____________________________________|
		 * |       3     |  Opposite direction  |
		 * |____________________________________|
		 * 
		 */
		
		int player_x = player.getPlayer_x();
		int player_y = player.getPlayer_y();
		double[] direction_arr;
		double angle;
		float speed_coeff = 0;
		
		// Loop through all enemies
		for(int enemy_index = 0; enemy_index < enemy_amount; enemy_index++)
		{
			Enemy current_enemy = enemies[enemy_index];
			
			// Assign speed-coefficient depending on "enemy-type"
			switch(current_enemy.follow_type)
			{
				case 0:
					speed_coeff = 1;
	//					speed_coeff = 0;
					break;
				case 1:
					speed_coeff = (float) 0.5;
	//					speed_coeff = 0;
					break;
				case 2:
					speed_coeff = game_timer.getRandom_speed_coeff();
	//					speed_coeff = 0;
					break;
				case 3:
					speed_coeff = -1;
	//					speed_coeff = 0;
					break;
			}
			
			double enemy_x = current_enemy.getEnemy_x();
			double enemy_y = current_enemy.getEnemy_y();
			
			int delta_x	   = player_x - (int) enemy_x;
			int delta_y    = player_y - (int) enemy_y;
			
			// Get direction needed to reach player
			try
			{			
				direction_arr = new double[]{delta_x / Math.abs(delta_x), delta_y / Math.abs(delta_y)};
				angle = Math.atan(Math.abs(delta_y) / Math.abs(delta_x));
			} catch(Exception e)
			{
				delta_x		  = 1;
				delta_y		  = 1;
				direction_arr = new double[]{delta_x / Math.abs(delta_x), delta_y / Math.abs(delta_y)};
				angle 		  = Math.atan(Math.abs(delta_y) / Math.abs(delta_x));
			}
			
			direction_arr[0] *= speed_coeff;
			direction_arr[1] *= speed_coeff;
			
			double speed_x = direction_arr[0] * current_enemy.max_speed * Math.cos(angle);
			double speed_y = direction_arr[1] * current_enemy.max_speed * Math.sin(angle);
			
			// Check enemy-enemy collision
			int[] enemy_collision_params = checkCollision( (int) (enemy_x + speed_x),
														   (int) (enemy_y + speed_y),
														   current_enemy.width,
														   current_enemy.height,
														   enemy_index);
			
			// Collision between enemies
			if(enemy_collision_params[0] == 1)
			{
				speed_x *= -1;
				speed_y *= -1;
			}

			// Enemy stays within map constraints
			if(moveableX2(enemy_x, enemy_y, current_enemy.width, current_enemy.height, direction_arr[0]))
				current_enemy.setEnemy_x((int) (enemy_x + speed_x));
			if(moveableY2(enemy_x, enemy_y, current_enemy.width, current_enemy.height, direction_arr[1]))
				current_enemy.setEnemy_y((int) (enemy_y + speed_y));
		}
	}

	// Moveable within x-direction
	private boolean moveableX2(double enemy_x, double enemy_y, int player_width, int player_height, double direction_arr)
	{
		if(direction_arr > 0)
			enemy_x += player_width;
		
		/* 
		 * 	  enemy_x	  enemy_x + player_width
		 * 	 		<--- --->
		 * 			
		 * (x, y1)  --------
		 *   		|      |
		 *   		|	   |
		 *   		|	   |
		 *   		|	   |
		 *   		|	   |
		 *  		|	   |
		 * (x, y2)  --------
		 * 
		 */
		
//		int y1 = map_constraints[(int) Enemy_y][(int) Enemy_x];
//		int y2 = map_constraints[(int) Enemy_y + player_height][(int) Enemy_x];
		
		for(int i = 1; i <= 13; i++)
		{
			enemy_x += direction_arr;
			// 				  			(x, y1)																	(x, y2)
			if(map_constraints[(int) enemy_y][(int) enemy_x] == 1 || map_constraints[(int) enemy_y + player_height][(int) enemy_x] == 1)
				return false;
		}
		
		return true;
	}

	// Moveable within y-direction
	private boolean moveableY2(double enemy_x, double enemy_y, int player_width, int player_height, double direction_arr)
	{
		if(direction_arr > 0)
			enemy_y += player_height;
		
		/* 
		 * 			
		 *		   (x1, y)  --------- (x2, y)
		 *  				|		|
		 *    			/\	|       |
		 *    			|	|	    |
		 *     player  	|	|	    |
		 *     height  	|	|	    |
		 *    			|	|	    |
		 *    			\/	|	    |
		 *          		----------
		 * 
		 */
		
//		int x1 = map_constraints[(int) Enemy_y][(int) Enemy_x];
//		int x2 = map_constraints[(int) Enemy_y][(int) Enemy_x + player_width];
		
		for(int i = 1; i <= 13; i++)
		{
			enemy_y += direction_arr;
			try
			{
				// 				  			(x1, y)																	(x2, y)
				if(map_constraints[(int) enemy_y][(int) enemy_x] == 1 || map_constraints[(int) enemy_y][(int) enemy_x + player_width] == 1)
					return false;
			} catch(Exception e){return false;}
		}
		
		return true;
	}
	
	// Check if entity collides with any enemy
	private int[] checkCollision(int entity_x, int entity_y, int entity_width, int entity_height, int current_index)
	{
		int[] ret_arr = {0, 0};
		
		int entity_mid_x = entity_x + (entity_width / 2);
		int entity_mid_y = entity_y + (entity_height / 2);
		
		for(int enemy_index = 0; enemy_index < enemy_amount; enemy_index++)
		{
			if(enemy_index != current_index)
			{				
				boolean x_crossed = false;
				boolean y_crossed = false;
				
				Enemy current_enemy = enemies[enemy_index];
				
				int enemy_x = current_enemy.getEnemy_x();
				int enemy_y = current_enemy.getEnemy_y();
				
				// Enemy left of entity
				if(enemy_x < entity_x && entity_mid_x < (enemy_x + current_enemy.width))
					x_crossed = true;
				// Enemy right of entity
				else if(entity_x < enemy_x && enemy_x < entity_mid_x)
					x_crossed = true;
				// Enemy above entity
				if(enemy_y < entity_y && entity_mid_y < (enemy_y + current_enemy.height))
					y_crossed = true;
				// Enemy below entity
				else if(entity_y < enemy_y && enemy_y < entity_mid_y)
					y_crossed = true;
				
				// Collision
				if(x_crossed && y_crossed)
				{
					ret_arr[0] = 1;
					ret_arr[1] = enemy_index;
					
					return ret_arr;
				}
			}
		}
		
		return ret_arr;
	}
	
	// Call and init. mini-game class
	private void launchMiniGame(int enemy_collision_index)
	{
		int player_x = player.getPlayer_x();
		int player_y = player.getPlayer_y();
		
		clearChildren();
		frame.remove(this);
		
		switch(enemy_collision_index)
		{
			case 0:
				new Lulle(frame, top, game_timer, key_handler, player_x, player_y);
				break;
			case 1:
				game_timer.setTime_coeff(-1);
				//			new Albin(player_x, player_y);
				break;
			case 2:
				//			new Lkab(player_x, player_y);
				break;
			case 3:
				//			new SSC(player_x, player_y);
				break;
			case 4:
				//			new Slusk(player_x, player_y);
				break;
			case 5:
				//			new Attila(player_x, player_y);
				break;
			case 6:
				new Pauline(frame, top, game_timer, key_handler, player_x, player_y);
				break;
		}
	}
	
	// Nullify all, to then be collected by the garbage collector,
	// a.k.a. clears memory
	private void clearChildren()
	{
		player 	= null;
		lulle 	= null;
		albin 	= null;
		lkab 	= null;
		ssc 	= null;
		slusk 	= null;
		attila 	= null;
		pauline = null;
		enemies = null;
	}
	
	// Load file with map boundaries
	private int[][] loadMapConstraints(int cols, int rows)
	{
		int[][] map = new int[rows][cols];
		int i = 0;
		int j = 0;
		
		try
		{
			File file = new File("map-4.txt");
			Scanner reader = new Scanner(file);
			while(reader.hasNextLine())
			{
				String current_line = reader.nextLine();
				char[] ch_current_line = current_line.toCharArray();
				for(int k = 0; k < ch_current_line.length; k++)
				{
					// Not boundary-element
					if(ch_current_line[k] == '0')
					{
						map[i][j] = 0;
						j++;
					}
					
					// Boundary-element
					else if(ch_current_line[k] == '1')
					{
						map[i][j] = 1;
						j++;
					}
				}
				i++;
				j = 0;
			}
			reader.close();
		} catch(Exception file_except)
		{
			file_except.printStackTrace();
		}
		
		return map;
	}

	@Override
	public void paintComponent(Graphics g_1d)
	{
		super.paintComponent(g_1d);
		
		Graphics2D g_2d = (Graphics2D) g_1d;
		
		if(game_loop_running)
			// Draw map
			draw(g_2d);
		
		if(game_loop_running)
		{			
			// Paint player
			try
			{			
				g_2d.setColor(Color.PINK);
				g_2d.fillRect(player.getPlayer_x(), player.getPlayer_y(), player.player_width, player.player_height);
			} catch(Exception e)
			{
				System.out.println(e.getCause());
			}
		}

		if(enemies_updating)
		{
			// Paint enemies
			for(int i = 0; i < enemy_amount; i++)
			{
				try
				{					
					Enemy current_Enemy = enemies[i];
					
					g_2d.setColor(Color.BLACK);
					g_2d.fillRect(current_Enemy.getEnemy_x(), current_Enemy.getEnemy_y(), current_Enemy.width, current_Enemy.height);
					g_2d.setColor(Color.WHITE);
					g_2d.drawString(current_Enemy.id_string, current_Enemy.getEnemy_x(), current_Enemy.getEnemy_y() + 15);
				} catch(Exception e)
				{
					System.out.println(e.getCause());
				}
			}
		}
		
		g_2d.dispose();
	}
	
	public void draw(Graphics2D g_2d)
	{
		g_2d.drawImage(map_img, 0, 0, this.width, this.height, null);
	}
}
