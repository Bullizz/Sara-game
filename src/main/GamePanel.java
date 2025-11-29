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
import javax.swing.JOptionPane;
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

public class GamePanel extends JPanel implements Runnable
{
	KeyHandler key_handler;
	Player Player;
	Enemy lulle, albin, lkab, ssc, slusk, attila, pauline;
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
	
	MouseList ml;
	
	public GamePanel(JFrame frame, /*int top_height*/ JLabel top, GameTimer game_timer, int Player_x0, int Player_y0)
	{
		super();
			this.width = frame.getWidth();
			this.height = 9 * (frame.getHeight() / 10);
		setPreferredSize(new Dimension(this.width, this.height));
		setLocation(0, 0);
		setFocusable(true);
		
		key_handler = new KeyHandler(frame);
		addKeyListener(key_handler);
		try
		{
			map_img = ImageIO.read(getClass().getResourceAsStream("/image_files/europe_4.png"));
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		
		frame.add(this);
		frame.setVisible(true);
		
		// Init. Player
		Player = new Player(972, 101, this.height / 40, this.width / 40);
		
		// Init. enemies
		lulle = new Enemy(	"lulle",	0,  1017,  359,  this.height / 40, this.width / 40);
		albin = new Enemy(	"albin",	3,  1468,  356,  this.height / 40, this.width / 40);
		lkab = new Enemy(	"lkab",		1,  664,   313,  this.height / 40, this.width / 40);
		ssc = new Enemy(	"ssc",		2,  1665,  107,  this.height / 40, this.width / 40);
		slusk = new Enemy(	"slusk",	1,  1402,  567,  this.height / 40, this.width / 40);
		attila = new Enemy(	"attila",	0,  838,   755,  this.height / 40, this.width / 40);
		pauline = new Enemy("pauline",	2,  504,   647,  this.height / 40, this.width / 40);
		
		enemies = new Enemy[]{lulle, albin, lkab, ssc, slusk, attila, pauline};
		
		this.frame = frame;
		this.top = top;
		map_constraints = loadMapConstraints(this.width, this.height);
		
//		ml = new MouseList(this.width, this.height, frame.getHeight() / 10);
//		frame.addMouseMotionListener(ml);
		
//		addMouseListener(new MouseAdapter() {
//			public void mousePressed(MouseEvent e)
//			{
//				JOptionPane.showMessageDialog(null, e.getX() + ", " + e.getY());
//			}
//		});
		
		initGameThread();
	}

	private void initGameThread()
	{
		if(game_timer == null)
		{
			game_timer = new GameTimer();
			game_timer.initTimer(top);
		}
		
		game_loop_running = true;
		
		game_thread = new Thread(this);
		game_thread.start();
	}
	
	int FPS = 60;
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
					
					/*
					 * Maybe add extra thread for enemies movement
					 */
//					updateEnemies();
					
//					top.setText(game_timer.getTime_str());

					repaint();
					
					// Player-Enemy collision
					int[] collision_params = checkCollision( Player.getPlayer_x(),
															 Player.getPlayer_y(),
															 Player.player_width,
															 Player.player_height,
															 -1);
					// Collision
					if(collision_params[0] == 1)
						initMiniGame(collision_params[1]);
					
					delta = 0;
					
					game_paused = key_handler.isGame_paused();
					if(game_paused)
					{
						game_timer.setTime_coeff(0);
//						GAME_MENU game_menu = new GAME_MENU(frame);
					}
					System.out.println("game-loop running");
				}
			} // End of slave game-loop	
		} // End of master game-loop
	}

	private void updatePlayer()
	{
		// Get key pressed
		int[] Player_direction_arr = key_handler.getDirection_arr();
		
		// Get positive/negative direction of Player
		int Player_dx = Player_direction_arr[0];
		int Player_dy = Player_direction_arr[1];
		
		// Get current speed of Player
		int Player_speed_x = Player.getPlayer_speed_x();
		int Player_speed_y = Player.getPlayer_speed_y();
		
		// Horizontal acceleration
		if((key_handler.LEFT || key_handler.RIGHT) && Player_speed_x < Player.max_speed)
			Player_speed_x++;
		else if((!key_handler.LEFT || !key_handler.RIGHT) && Player_speed_x > 0)
			Player_speed_x--;

		// Vertical acceleration
		if((key_handler.UP || key_handler.DOWN) && Player_speed_y < Player.max_speed)
			Player_speed_y++;
		else if((!key_handler.UP || !key_handler.DOWN) && Player_speed_y > 0)
			Player_speed_y--;
		
		// Get position of Player
		int Player_x = Player.getPlayer_x();
		int Player_y = Player.getPlayer_y();
		
		// If within map constraints
		if(moveableX2(Player_x, Player_y, Player.player_width, Player.player_height, Player_dx))
			Player_x += Player_speed_x * Player_dx;
		if(moveableY2(Player_x, Player_y, Player.player_width, Player.player_height, Player_dy))
			Player_y += Player_speed_y * Player_dy;
		
		Player.setPlayer_x(Player_x);
		Player.setPlayer_y(Player_y);
		Player.setPlayer_speed_x(Player_speed_x);
		Player.setPlayer_speed_y(Player_speed_y);
	}

	private boolean moveableX2(double Enemy_x, double Enemy_y, int Player_width, int Player_height, double direction_arr)
	{
		if(direction_arr > 0)
			Enemy_x += Player_width;
		
		
		for(int i = 1; i <= 13; i++)
		{
			Enemy_x += direction_arr;
			if(map_constraints[(int) Enemy_y][(int) Enemy_x] == 1 || map_constraints[(int) Enemy_y + Player_height][(int) Enemy_x] == 1)
				return false;
		}
		
		return true;
	}
	private boolean moveableY2(double Enemy_x, double Enemy_y, int Player_width, int Player_height, double direction_arr)
	{
		if(direction_arr > 0)
			Enemy_y += Player_height;
		
		for(int i = 1; i <= 13; i++)
		{
			Enemy_y += direction_arr;
			try
			{
				if(map_constraints[(int) Enemy_y][(int) Enemy_x] == 1 || map_constraints[(int) Enemy_y][(int) Enemy_x + Player_width] == 1)
					return false;
			} catch(Exception e)
			{
				return false;
			}
		}
		
		return true;
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
		
		int Player_x = Player.getPlayer_x();
		int Player_y = Player.getPlayer_y();
		double[] direction_arr;
		double angle;
		float speed_coeff = 0;
		
		// Loop through all enemies
		for(int Enemy_index = 0; Enemy_index < enemies.length; Enemy_index++)
		{
			Enemy current_Enemy = enemies[Enemy_index];
			
			// Assign speed-coefficient depending on "Enemy-type"
			switch(current_Enemy.follow_type)
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
			
			double Enemy_x = current_Enemy.getEnemy_x();
			double Enemy_y = current_Enemy.getEnemy_y();
			
			int delta_x = Player_x - (int) Enemy_x;
			int delta_y = Player_y - (int) Enemy_y;
			
			// Get direction needed to reach Player
			try
			{			
				direction_arr = new double[]{delta_x / Math.abs(delta_x), delta_y / Math.abs(delta_y)};
				angle = Math.atan(Math.abs(delta_y) / Math.abs(delta_x));
			} catch(Exception e)
			{
				delta_x = 1;
				delta_y = 1;
				direction_arr = new double[]{delta_x / Math.abs(delta_x), delta_y / Math.abs(delta_y)};
				angle = Math.atan(Math.abs(delta_y) / Math.abs(delta_x));
			}
			
			direction_arr[0] *= speed_coeff;
			direction_arr[1] *= speed_coeff;
			
			double speed_x = direction_arr[0] * current_Enemy.max_speed * Math.cos(angle);
			double speed_y = direction_arr[1] * current_Enemy.max_speed * Math.sin(angle);
			
			// Check Enemy-Enemy collision
			int[] Enemy__collision_params = checkCollision( (int) (Enemy_x + speed_x),
														    (int) (Enemy_y + speed_y),
														     current_Enemy.width,
														     current_Enemy.height,
														     Enemy_index);
			if(Enemy__collision_params[0] == 1)
			{
				// Enemy stays within map constraints
				if(moveableX2( Enemy_x, Enemy_y, current_Enemy.width, current_Enemy.height, direction_arr[0]))
					current_Enemy.setEnemy_x((int) (Enemy_x + speed_x));
				if(moveableY2(Enemy_x, Enemy_y, current_Enemy.width, current_Enemy.height, direction_arr[0]))
					current_Enemy.setEnemy_y((int) (Enemy_y + speed_y));
			}
			

			if(current_Enemy.id_string.equals("pauline") || current_Enemy.id_string.equals("ssc"))
			{
				if(0 > current_Enemy.getEnemy_x() || current_Enemy.getEnemy_x() > this.width)
				{
					frame.setTitle("BRUH");
					System.out.println();
				}
				if(0 > current_Enemy.getEnemy_y() || current_Enemy.getEnemy_y() > this.height)
				{
					frame.setTitle("BRUH");					
					System.out.println();
				}
			}
		}
	}
	
	// Methods to check if entity is within frame
	private boolean moveableX(int x, int width, double dx)
	{
		if(dx > 0 && x + width < this.width - (width / 2))
			return true;
		else if(dx < 0 && (width / 2) < x)
			return true;
		else
			return false;
	}
	private boolean moveableY(int y, int height, double dy)
	{
		if(dy > 0 && y + height < this.height - (height / 1))
			return true;
		else if(dy < 0 && y > (height / 2))
			return true;
		else
			return false;
	}
	
	// Check if entity collides with any Enemy
	private int[] checkCollision(int entity_x, int entity_y, int entity_width, int entity_height, int current_index)
	{
		int[] ret_arr = {0, 0};
		
		int entity_mid_x = entity_x + (entity_width / 2);
		int entity_mid_y = entity_y + (entity_height / 2);
		
		for(int Enemy_index = 0; Enemy_index < enemies.length; Enemy_index++)
		{
			if(Enemy_index != current_index)
			{				
				boolean x_crossed = false;
				boolean y_crossed = false;
				
				Enemy current_Enemy = enemies[Enemy_index];
				
				int Enemy_x = current_Enemy.getEnemy_x();
				int Enemy_y = current_Enemy.getEnemy_y();
				
				// Enemy left of entity
				if(Enemy_x < entity_x && entity_mid_x < (Enemy_x + current_Enemy.width))
					x_crossed = true;
				// Enemy right of entity
				else if(entity_x < Enemy_x && Enemy_x < entity_mid_x)
					x_crossed = true;
				// Enemy above entity
				if(Enemy_y < entity_y && entity_mid_y < (Enemy_y + current_Enemy.height))
					y_crossed = true;
				// Enemy below entity
				else if(entity_y < Enemy_y && Enemy_y < entity_mid_y)
					y_crossed = true;
				
				// Collision
				if(x_crossed && y_crossed)
				{
					ret_arr[0] = 1;
					ret_arr[1] = Enemy_index;
					
					return ret_arr;
				}
			}
		}
		return ret_arr;
	}
	
	private void initMiniGame(int Enemy_index)
	{
		frame.remove(this);
		frame.repaint();
			String s = null;
		int Player_x = Player.getPlayer_x();
		int Player_y = Player.getPlayer_y();
		clearChildren();
		game_loop_running = false;
		switch(Enemy_index)
		{
			case 0:		// lulle
				s = "lulle";
				new Lulle(frame, Player_x, Player_y);
				break;
			case 1:		// albin
				s = "albin";
				game_timer.setTime_coeff(-1);
				new Albin(Player_x, Player_y);
				break;
			case 2:		// lkab
				s = "lkab";
//				game_timer.setTime_coeff(10);
				new Lkab(Player_x, Player_y);
				break;
			case 3:		// ssc
				s = "ssc";
				new SSC(Player_x, Player_y);
				break;
			case 4:		// slusk
				s = "slusk";
				game_timer.setTime_coeff(200);
				new Slusk(Player_x, Player_y);
				break;
			case 5:		// attila
				s = "attila";
				int random_time = 10 * ((int) game_timer.getRandom_speed_coeff() + 1);
				game_timer.setTime_coeff(random_time);
				new Attila(Player_x, Player_y);
				break;
			case 6:		// pauline
				s = "pauline";
				new Pauline(Player_x, Player_y);
				break;
		}
		JOptionPane.showMessageDialog(null, s);
	}
	
	// Nullify all to then becollected by the garbage collector,
	// clears memory
	private void clearChildren()
	{
		Player 	= null;
		lulle 	= null;
		albin 	= null;
		lkab 	= null;
		ssc 	= null;
		slusk 	= null;
		attila 	= null;
		pauline = null;
		enemies = null;
	}

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
				String curr_line = reader.nextLine();
				char[] ch_curr_line = curr_line.toCharArray();
				for(int k = 0; k < ch_curr_line.length; k++)
				{
					if(ch_curr_line[k] == '0')// && j < this.width)
					{
						map[i][j] = 0;
						j++;
					}
					else if(ch_curr_line[k] == '1')// && j < this.width)
					{
						map[i][j] = 1;
						j++;
					}
//					System.out.println(ch_curr_line[k]);
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
		
		if(!map_drawn)
		{
			map_drawn = true;
		}
		draw(g_2d);
		
		// Paint Player
		g_2d.setColor(Color.PINK);
		g_2d.fillRect(Player.getPlayer_x(), Player.getPlayer_y(), Player.player_width, Player.player_height);
		
		// Paint enemies
		for(int i = 0; i < enemies.length; i++)
		{
			Enemy current_Enemy = enemies[i];
			
			g_2d.setColor(Color.BLACK);
			g_2d.fillRect(current_Enemy.getEnemy_x(), current_Enemy.getEnemy_y(), current_Enemy.width, current_Enemy.height);
			g_2d.setColor(Color.WHITE);
			g_2d.drawString(current_Enemy.id_string, current_Enemy.x, current_Enemy.y + 15);
		}
		g_2d.dispose();
	}
	
	public void draw(Graphics2D g_2d)
	{
		g_2d.drawImage(map_img, 0, 0, this.width, this.height, null);
	}
}