package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import entities.ENEMY;
import entities.PLAYER;
import menu.GAME_MENU;

public class GAME_PANEL extends JPanel implements Runnable
{
	KEY_HANDLER key_handler;
	PLAYER player;
	ENEMY lulle, albin, lkab, ssc, slusk, attila, pauline;
	ENEMY[] enemies;
	GAME_TIMER game_timer = new GAME_TIMER();
	
	Thread game_thread;
	
	final int width;
	final int height;
	
	boolean game_loop_running, game_paused;
	double random_speed_coeff;
	
	JFrame frame;
	
	public GAME_PANEL(JFrame frame)
	{
		super();	
			this.width = frame.getWidth();
			this.height = frame.getHeight();
		setSize(frame.getSize());
		setLocation(0, 0);
		key_handler = new KEY_HANDLER(frame);
		addKeyListener(key_handler);
		setFocusable(true);
		setBackground(Color.RED);
		
		frame.add(this);
		frame.setVisible(true);
		
		// Init. player
		player = new PLAYER(width / 2, 10, 108 / 2, 192 / 2);
		
		// Init. enemies
		lulle = new ENEMY(	"lulle",	0,  1 * (this.width / 9), 4 * (this.height / 6), 108 / 2, 192 / 2);
		lkab = new ENEMY(	"lkab",		1,  2 * (this.width / 9), 5 * (this.height / 6), 108 / 2, 192 / 2);
		albin = new ENEMY(	"albin",	3,  3 * (this.width / 9), 4 * (this.height / 6), 108 / 2, 192 / 2);
		ssc = new ENEMY(	"ssc",		2,  4 * (this.width / 9), 5 * (this.height / 6), 108 / 2, 192 / 2);
		slusk = new ENEMY(	"slusk",	1,  5 * (this.width / 9), 4 * (this.height / 6), 108 / 2, 192 / 2);
		attila = new ENEMY(	"attila",	0,  6 * (this.width / 9), 5 * (this.height / 6), 108 / 2, 192 / 2);
		pauline = new ENEMY("pauline",	2,  7 * (this.width / 9), 5 * (this.height / 6), 108 / 2, 192 / 2);
		enemies = new ENEMY[]{lulle, albin, lkab, ssc, slusk, attila, pauline};
		
		this.frame = frame;
		
		initGameThread();
	}

	private void initGameThread()
	{
		game_timer.initTimer();
		
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
			if(!game_paused)
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
					
					updateEnemies();
					
					repaint();
					
					// Player-enemy collision
					if(checkCollision(player.getPlayer_x(),
									  player.getPlayer_y(),
									  player.player_width,
									  player.player_height,
									  -1))
					{
//						 JOptionPane.showMessageDialog(null, "Collision");
//						 System.exit(0);
					}
					
					delta = 0;
					
					game_paused = key_handler.isGame_paused();
					if(game_paused)
					{
						game_timer.setTime_coeff(0);
//						GAME_MENU game_menu = new GAME_MENU(frame);
					}
				}
			} // End of slave game-loop	
		} // End of master game-loop
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
		
		// If within frame
		if(moveableX(player_x, player.player_width, player_dx))
			player_x += player_speed_x * player_dx;
		if(moveableY(player_y, player.player_height, player_dy))
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
		for(int enemy_index = 0; enemy_index < enemies.length; enemy_index++)
		{
			ENEMY current_enemy = enemies[enemy_index];
			
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
			
			int delta_x = player_x - (int) enemy_x;
			int delta_y = player_y - (int) enemy_y;
			
			// Get direction needed to reach player
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
			
			double speed_x = direction_arr[0] * current_enemy.max_speed * Math.cos(angle);
			double speed_y = direction_arr[1] * current_enemy.max_speed * Math.sin(angle);
			
			// Check enemy-enemy collision
			if(!checkCollision( (int) (enemy_x + speed_x),
							    (int) (enemy_y + speed_y),
							     current_enemy.width,
							     current_enemy.height,
							     enemy_index))
			{				
				enemy_x += speed_x;
				enemy_y += speed_y;
			}
			else
			{				
//				enemy_x -= speed_x;
//				enemy_y -= speed_y;
			}
			
			// Enemy stays within frame
			if(moveableX( (int) enemy_x, current_enemy.width, direction_arr[0]))
				current_enemy.setEnemy_x((int) enemy_x);
			if(moveableY( (int) enemy_y, current_enemy.height, direction_arr[1]))
				current_enemy.setEnemy_y((int) enemy_y);

			if(current_enemy.id_string.equals("pauline") || current_enemy.id_string.equals("ssc"))
			{
				if(0 > current_enemy.getEnemy_x() || current_enemy.getEnemy_x() > this.width)
				{
					frame.setTitle("BRUH");
					System.out.println();
				}
				if(0 > current_enemy.getEnemy_y() || current_enemy.getEnemy_y() > this.height)
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
		if(dy > 0 && y + height < this.height - (height / 2))
			return true;
		else if(dy < 0 && y > (height / 2))
			return true;
		else
			return false;
	}
	
	//
/*	private boolean checkEnemiesCollisionX(int x, int width, double dx, int current_enemy_index)
	{
		int enemy_counter = 0;
		int valid_enemy_position = 0;
		
		for(int i = 0; i < enemies.length; i++)
		{
			if(i != current_enemy_index)
			{
				enemy_counter++;
				// Check enemies right
				if(dx > 0 && x + width < enemies[i].getEnemy_x())
					valid_enemy_position++;
				// Check enemies left
				else if(dx < 0 && enemies[i].getEnemy_x() + enemies[i].width < x)
					valid_enemy_position++;
			}
		}
		if(enemy_counter == valid_enemy_position)
			return false;
		else
			return true;
	}
*/
	
	//
//	private boolean checkEnemiesCollisionY(int y, int height, double dy, int current_enemy_index)
//	{
//		int enemy_counter = 0;
//		int valid_enemy_position = 0;
//		
//		for(int i = 0; i < enemies.length; i++)
//		{
//			if(i != current_enemy_index)
//			{
//				enemy_counter++;
//				// Check enemies above
//				if(dy > 0 && y + height < enemies[i].getEnemy_y())
//					valid_enemy_position++;
//				// Check enemies below
//				else if(dy < 0 && enemies[i].getEnemy_y() + enemies[i].height < y)
//					valid_enemy_position++;
//			}
//		}
//		if(enemy_counter == valid_enemy_position)
//			return false;
//		else
//			return true;
//	}
	
	// Check if entity collided with any enemy
	private boolean checkCollision(int entity_x, int entity_y, int entity_width, int entity_height, int current_index)
	{
		int entity_mid_x = entity_x + (entity_width / 2);
		int entity_mid_y = entity_y + (entity_height / 2);
		
		for(int enemy_index = 0; enemy_index < enemies.length; enemy_index++)
		{
			if(enemy_index != current_index)
			{				
				boolean x_crossed = false;
				boolean y_crossed = false;
				
				ENEMY current_enemy = enemies[enemy_index];
				
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
					return true;
			}
		}
		return false;
	}
	
	public void paintComponent(Graphics g_1d)
	{
		super.paintComponent(g_1d);
		
		// Paint player
		Graphics2D g_2d = (Graphics2D) g_1d;
		g_2d.setColor(Color.PINK);
		g_2d.fillRect(player.getPlayer_x(), player.getPlayer_y(), player.player_width, player.player_height);
		
		// Paint enemies
		for(int i = 0; i < enemies.length; i++)
		{
			ENEMY current_enemy = enemies[i];
			
			g_2d.setColor(Color.BLACK);
			g_2d.fillRect(current_enemy.getEnemy_x(), current_enemy.getEnemy_y(), current_enemy.width, current_enemy.height);
			g_2d.setColor(Color.WHITE);
			g_2d.drawString(current_enemy.id_string, current_enemy.x, current_enemy.y + 15);
		}
		
		g_2d.dispose();
	}
}