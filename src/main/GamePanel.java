package main;

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

import menu.EndMenu;
import menu.StartMenu;

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
	final int enemy_amount = 7;
	
	Enemy[] enemies;
	GameTimer game_timer;
	AudioHandler game_audio;
	
	Thread game_thread, enemies_thread;
	
	final int width;
	final int height;
	
	boolean game_loop_running, enemies_updating;
	double random_speed_coeff;
	
	JFrame frame;
	JLabel top;
	
	BufferedImage map_img, player_img;
	BufferedImage lulle_img, albin_img, lkab_img, ssc_img, slusk_img, attila_img, pauline_img;
	
	int[][] map_constraints;
	
	int enemy_collision_index = -1;
	
	int GOAL_X = 854;
	int GOAL_Y = 772;
	
	public GamePanel(JFrame frame, JLabel top, GameTimer game_timer, KeyHandler key_handler, AudioHandler game_audio, int player_x0, int player_y0)
	{
		super();
			this.width 	= frame.getWidth();
			this.height = 9 * (frame.getHeight() / 10);
		setPreferredSize(new Dimension(this.width, this.height));
		setLocation(0, 0);
		setFocusable(false);
		
		try
		{
			map_img		= ImageIO.read(getClass().getResourceAsStream("/image_files/GamePanel/world_map.png"));
			player_img	= ImageIO.read(getClass().getResourceAsStream("/image_files/GamePanel/player.png"));
			
			lulle_img	= ImageIO.read(getClass().getResourceAsStream("/image_files/GamePanel/lulle.png")); 
			albin_img	= ImageIO.read(getClass().getResourceAsStream("/image_files/GamePanel/albin.png"));
			lkab_img	= ImageIO.read(getClass().getResourceAsStream("/image_files/GamePanel/lkab.png"));
			ssc_img		= ImageIO.read(getClass().getResourceAsStream("/image_files/GamePanel/ssc.png"));
			slusk_img	= ImageIO.read(getClass().getResourceAsStream("/image_files/GamePanel/slusk.png"));
			attila_img	= ImageIO.read(getClass().getResourceAsStream("/image_files/GamePanel/attila.png"));
			pauline_img	= ImageIO.read(getClass().getResourceAsStream("/image_files/GamePanel/pauline.png"));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		frame.add(this);
		
		int entity_width  = this.width  / 30;
		int entity_height = this.height / 30;
		
		// Init. enemies
		lulle 	= new Enemy("lulle",	1460,  259,  entity_height, entity_width);
		albin	= new Enemy("albin",	1468,  356,  entity_height, entity_width);
		lkab	= new Enemy("lkab",		664,   313,  entity_height, entity_width);
		ssc		= new Enemy("ssc",		1645,  100,  entity_height, entity_width);
		slusk	= new Enemy("slusk",	1402,  567,  entity_height, entity_width);
		attila	= new Enemy("attila",	807,   739,  entity_height, entity_width);
		pauline = new Enemy("pauline",  504,   647,  entity_height, entity_width);
		
		enemies = new Enemy[]{lulle, albin, lkab, ssc, slusk, attila, pauline};

		// Check that player-spawn != any enemy-spawn
		int buffer_zone = 10;
		int enemy_i = 0;
		while(enemy_i < enemy_amount)
		{
			boolean valid_x = false;			
			boolean valid_y = false;
			
			// Enemy to the right
			int x_diff_1 = Math.abs(enemies[enemy_i].getEnemy_x() - (player_x0 + entity_width));
			// Enemy to the left			
			int x_diff_2 = Math.abs(player_x0 - (enemies[enemy_i].getEnemy_x() + entity_width));
			
			// Enemy below player
			int y_diff_1 = Math.abs(enemies[enemy_i].getEnemy_y() - (player_y0 + entity_height));
			// Enemy above player
			int y_diff_2 = Math.abs(player_y0 - (enemies[enemy_i].getEnemy_y() + entity_height));
			
			if(x_diff_1 <= buffer_zone)
				player_x0 -= buffer_zone;
			else if(x_diff_2 <= buffer_zone)
				player_x0 += buffer_zone;
			else
				valid_x = true;
				
			if(y_diff_1 <= buffer_zone)
				player_y0 -= buffer_zone;
			else if(y_diff_2 <= buffer_zone)
				player_y0 += buffer_zone;
			else
				valid_y = true;
			
			if(valid_x && valid_y)
				enemy_i++;
		}
			
		// Init. player
		player	= new Player(player_x0, player_y0, entity_height, entity_width);
		
		this.frame 		 = frame;
		this.top 		 = top;
		this.key_handler = key_handler;
		this.game_timer  = game_timer;
		this.game_audio  = game_audio;
		
		// Integer matrix with map boundaries
		map_constraints = loadMapConstraints(this.width, this.height);
		
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
		game_timer.setTime_coeff(1);
		
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
					
					// Slave game-loop
					while(game_loop_running)
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
								killThreads();
								
								enemy_collision_index = collision_params[1];
							}
							
							// Reach goal y-pos.
							if(player.getPlayer_y() < GOAL_Y && GOAL_Y < (player.getPlayer_y() + player.player_height))
							{
								// Reach goal x-pos.
								if(player.getPlayer_x() < GOAL_X && GOAL_X < (player.getPlayer_x() + player.player_width))
								{	
									killThreads();

									game_timer.setTime_coeff(0);
								}
							}
							
							if(key_handler.GamePanel_esc_pressed)
								killThreads();
							
							delta = 0;
						}
					} // End of slave game-loop
					
					// Collision detected
					if(enemy_collision_index > -1)
						launchMiniGame(enemy_collision_index);
					
					// Player exit
					else if(key_handler.GamePanel_esc_pressed)
						initStartMenu();
					
					// No collision, game won
					else
						endGame();
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
					
					boolean anti_spawn  = true;
					int spawn_counter   = 0;
					
					// Slave game-loop
					while(enemies_updating)
					{
						current_time = System.nanoTime();
						delta += (current_time - last_time) / draw_interval;
						last_time = current_time;
						if(delta >= 1)
						{
							updateEnemies();
							
							// Enemies move away from player for first 15 frames
							if(anti_spawn)
							{
//								0, 3, 1, 2, 1, 0, 2
								spawn_counter++;
								if(spawn_counter % 15 == 0)
								{
									enemies[0].setFollow_type(0);
									enemies[1].setFollow_type(3);
									enemies[2].setFollow_type(1);
									enemies[3].setFollow_type(2);
									enemies[4].setFollow_type(1);
									enemies[5].setFollow_type(0);
									enemies[6].setFollow_type(2);
									
									anti_spawn = false;
								}
							}
							
							delta = 0;
						}
					} // End of [enemy] slave game-loop
				} // End of [enemy] master game-loop
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
		if((key_handler.LEFT || key_handler.RIGHT) && player_speed_x <= player.max_speed)
			player_speed_x++;
		else if((!key_handler.LEFT || !key_handler.RIGHT) && player_speed_x > 0)
			player_speed_x--;

		// Vertical acceleration
		if((key_handler.UP || key_handler.DOWN) && player_speed_y <= player.max_speed)
			player_speed_y++;
		else if((!key_handler.UP || !key_handler.DOWN) && player_speed_y > 0)
			player_speed_y--;
		
		// Get position of player
		int player_x = player.getPlayer_x();
		int player_y = player.getPlayer_y();
		
		// If within map constraints
		if(moveableX(player_x, player_y, player.player_width, player.player_height, player_dx) == 1)
			player_x += player_speed_x * player_dx;
		if(moveableY(player_x, player_y, player.player_width, player.player_height, player_dy) == 1)
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
		 * |       0     | Follow at 90% speed  |
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
			switch(current_enemy.getFollow_type())
			{
				case 0:
					speed_coeff = (float) 0.8;
					break;
				case 1:
					speed_coeff = (float) 0.5;
					break;
				case 2:
					speed_coeff = game_timer.getRandom_speed_coeff();
					break;
				case 3:
					speed_coeff = -1;
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
			int moving_x = moveableX(enemy_x, enemy_y, current_enemy.width, current_enemy.height, direction_arr[0]);
			int moving_y = moveableY(enemy_x, enemy_y, current_enemy.width, current_enemy.height, direction_arr[1]);
			
			if(moving_x == 1)
				enemy_x += speed_x;
			if(moving_y == 1)
				enemy_y += speed_y;
			if(moving_x == -1 || moving_y == -1)
			{				
				enemy_x = current_enemy.x0;
				enemy_y = current_enemy.y0;
			}
			
			current_enemy.setEnemy_x((int) enemy_x);
			current_enemy.setEnemy_y((int) enemy_y);
		}
	}

	// Moveable within x-direction
	private int moveableX(double entity_x, double entity_y, int entity_width, int entity_height, double direction_arr)
	{
		if(direction_arr > 0)
			entity_x += entity_width;
		
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
		
		for(int i = 1; i <= 13; i++)
		{
			entity_x += direction_arr;
			
			try
			{				
				// 				  			   (x, y1)																   (x, y2)
				if(map_constraints[(int) entity_y][(int) entity_x] == 1 || map_constraints[(int) entity_y + entity_height][(int) entity_x] == 1)
					return 0;
			} catch(Exception e)
			{
				return -1;
			}
		}
		
		return 1;
	}

	// Moveable within y-direction
	private int moveableY(double entity_x, double entity_y, int entity_width, int entity_height, double direction_arr)
	{
		if(direction_arr > 0)
			entity_y += entity_height;
		
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
		
		for(int i = 1; i <= 13; i++)
		{
			entity_y += direction_arr;
			try
			{
				// 				  			  (x1, y)												  (x2, y)
				if(map_constraints[(int) entity_y][(int) entity_x] == 1 || map_constraints[(int) entity_y][(int) entity_x + entity_width] == 1)
					return 0;
			} catch(Exception e)
			{
				return -1;
			}
		}
		
		return 1;
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
		
		key_handler.setDirection_arr(new int[] {0, 0});
		frame.remove(this);
		
		switch(enemy_collision_index)
		{
			case 0:
				new Lulle(frame, top, game_timer, key_handler, game_audio, player_x, player_y);
				break;
			case 1:
				game_timer.setTime_coeff(-1);
				new Albin(frame, top, game_timer, key_handler, game_audio, player_x, player_y);
				break;
			case 2:
				new Lkab(frame, top, game_timer, key_handler, game_audio, player_x, player_y);
				break;
			case 3:
				new SSC(frame, top, game_timer, key_handler, game_audio, player_x, player_y);
				break;
			case 4:
				game_timer.setTime_coeff(25);
				new Slusk(frame, top, game_timer, key_handler, game_audio, player_x, player_y);
				break;
			case 5:
				new Attila(frame, top, game_timer, key_handler, game_audio, player_x, player_y);
				break;
			case 6:
				new Pauline(frame, top, game_timer, key_handler, game_audio, player_x, player_y);
				break;
		}
	}
	
	// Load file with map boundaries
	private int[][] loadMapConstraints(int cols, int rows)
	{
		int[][] map = new int[rows][cols];
		int i = 0;
		int j = 0;
		
		try
		{
			File file = new File("map.txt");
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

	private void killThreads()
	{
		game_loop_running	= false;
		game_thread			= null;
		
		enemies_updating	= false;
		enemies_thread		= null;
	}
	
	// End GamePanel
	private void endGame()
	{
		String final_time_str = game_timer.getTime_str();
		game_timer.timer.cancel();
		
		frame.removeKeyListener(key_handler);
		frame.remove(this);
		
		new EndMenu(frame, top, game_audio, final_time_str, "Good Job!");
	}
	
	private void initStartMenu()
	{
		killThreads();
		game_timer.timer.cancel();
		
		frame.removeKeyListener(key_handler);
		frame.remove(this);

		top.setText("Vada a Bordo, Cazzo!");
		
		new StartMenu(frame, top, game_audio);
	}

	@Override
	public void paintComponent(Graphics g_1d)
	{
		super.paintComponent(g_1d);
		
		Graphics2D g_2d = (Graphics2D) g_1d;
		
		// Draw background
		g_2d.drawImage(map_img, 0, 0, this.width, this.height, null);
		
		// Paint player
		if(player != null)
			g_2d.drawImage(player_img, player.getPlayer_x(), player.getPlayer_y(), player.player_width, player.player_height, null);
		
		if(enemies_updating)
		{
			// Paint enemies
			for(int i = 0; i < enemy_amount; i++)
			{
				Enemy current_enemy = enemies[i];
				
				switch(current_enemy.id_string)
				{
				case "lulle":
					g_2d.drawImage(lulle_img,   current_enemy.getEnemy_x(), current_enemy.getEnemy_y(), current_enemy.width, current_enemy.height, null);
					break;
				case "albin":
					g_2d.drawImage(albin_img,   current_enemy.getEnemy_x(), current_enemy.getEnemy_y(), current_enemy.width, current_enemy.height, null);
					break;
				case "lkab":
					g_2d.drawImage(lkab_img,    current_enemy.getEnemy_x(), current_enemy.getEnemy_y(), current_enemy.width, current_enemy.height, null);
					break;
				case "ssc":
					g_2d.drawImage(ssc_img,     current_enemy.getEnemy_x(), current_enemy.getEnemy_y(), current_enemy.width, current_enemy.height, null);
					break;
				case "slusk":
					g_2d.drawImage(slusk_img,   current_enemy.getEnemy_x(), current_enemy.getEnemy_y(), current_enemy.width, current_enemy.height, null);
					break;
				case "attila":
					g_2d.drawImage(attila_img,  current_enemy.getEnemy_x(), current_enemy.getEnemy_y(), current_enemy.width, current_enemy.height, null);
					break;
				case "pauline":
					g_2d.drawImage(pauline_img, current_enemy.getEnemy_x(), current_enemy.getEnemy_y(), current_enemy.width, current_enemy.height, null);
					break;
				}
			}
		}
		
		g_2d.dispose();
	}
}