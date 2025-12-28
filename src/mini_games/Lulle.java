package mini_games;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import entities.Enemy;
import entities.Player;
import main.GamePanel;
import main.GameTimer;
import main.KeyHandler;

public class Lulle extends JPanel implements Runnable
{
	// Arguments
	JFrame frame;
	JLabel top;
	GameTimer game_timer;
	KeyHandler key_handler;
	int player_x_passing;
	int player_y_passing;
	
	Thread lulle_thread;
	int width, height;
	boolean game_paused 	  = false;
	boolean dirt_placed 	  = false;
	boolean game_loop_running = true;
	final int RNG 			  = 100;
	// 1 in <RNG> chance that dirt is generated ~every millisecond
	
	int MAX_POINTS  = 4;
	int points 		= 0;
	
	// General entity parameters
	int entity_width	 = 112;
	int entity_height 	 = 194;
	int entity_max_speed = 6;

	// Dirt parameters
	int dirt_width 	= entity_width;
	int dirt_heigth = entity_height / 2;
	int dirt_x 		= 0;
	int dirt_y 		= 0;
	BufferedImage dirt_img;
	
	// Player parameters
	Player player;
	BufferedImage player_img;
	
	// NPC parameters
	Enemy npc;
	// Randomize npc speed direction
	int[] speed_direction_arr = {-1, 1};
	int index 			  = (int) (Math.random() * 2);
	int index2 			  = (int) (Math.random() * 2);
	BufferedImage npc_img;
	
	public Lulle(JFrame frame, JLabel top, GameTimer game_timer, KeyHandler key_handler, int player_x, int player_y)
	{
		super();
			this.width 	= frame.getWidth();
			this.height = 9 * (frame.getHeight() / 10);
		setPreferredSize(new Dimension(this.width, this.height));
		setLocation(0, 0);
		setFocusable(false);
		setBackground(new Color(32, 89, 255));
		
		this.frame				= frame;
		this.top				= top;
		this.game_timer			= game_timer;
		this.key_handler		= key_handler;
		this.player_x_passing	= player_x;
		this.player_y_passing	= player_y;
	
		player = new Player(1689, 584, entity_width, entity_height);
		npc = new Enemy((String) null, 0, 121, 193, entity_width, entity_height);
		
		int npc_speed_x = speed_direction_arr[index];
		int npc_speed_y = speed_direction_arr[index2];
		npc.setSpeed_x(npc_speed_x);
		npc.setSpeed_y(npc_speed_y);
		
		try
		{
			player_img	= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/lulle/player_1-transp.png"));
			npc_img		= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/lulle/lulle_1-transp.png"));
			dirt_img	= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/lulle/dirt-transp.png"));
		} catch(IOException e)
		{
			System.err.println("Err ImageIO.read()");
			e.printStackTrace();
		}
		
		frame.add(this);
		frame.repaint();
		
		initLulleThread();
	}

	private void initLulleThread()
	{
		lulle_thread = new Thread(this);
		lulle_thread.start();
	}

	int FPS = 60;
	@Override
	public void run()
	{
		// Master game-loop
		while(lulle_thread != null)
		{
			double draw_interval = Math.pow(10, 9);
			draw_interval /= FPS;
			double delta = 0;
			long last_time = System.nanoTime();
			long current_time;
			
			boolean game_paused = key_handler.isGame_paused();
			
			// Slave game-loop
			while(game_loop_running && !game_paused)
			{
				current_time = System.nanoTime();
				delta += (current_time - last_time) / draw_interval;
				last_time = current_time;
				if(delta >= 1)
				{
					updatePlayer();
					
					updateNPC();
					
					if(dirt_placed)
						updateCleaning();

					repaint();
					
					// Minigame finished
					if(points == MAX_POINTS)
					{
						lulle_thread = null;
						frame.remove(this);
						game_loop_running = false;
					}
						
					delta = 0;
				}
			} // End of slave game-loop
		} // End of master game-loop
		new GamePanel(frame, top, game_timer, key_handler, player_x_passing, player_y_passing);
	}
	
	private void updatePlayer()
	{
		int[] direction_arr = key_handler.getDirection_arr();
		
		// Get positive/negative direction of player
		int player_dx = direction_arr[0];
		int player_dy = direction_arr[1];
		
		int player_speed_x = player.getPlayer_speed_x();
		int player_speed_y = player.getPlayer_speed_y();
		
		// Horizontal acceleration
		if((key_handler.LEFT || key_handler.RIGHT) && player_speed_x < entity_max_speed)
			player_speed_x++;
		else if((!key_handler.LEFT || !key_handler.RIGHT) && player_speed_x > 0)
			player_speed_x--;

		// Vertical acceleration
		if((key_handler.UP || key_handler.DOWN) && player_speed_y < entity_max_speed)
			player_speed_y++;
		else if((!key_handler.UP || !key_handler.DOWN) && player_speed_y > 0)
			player_speed_y--;
		
		int player_x = player.getPlayer_x();
		int player_y = player.getPlayer_y();
		
		// If within map constraints
		if(moveableX(player_x, player_dx))
			player_x += player_speed_x * player_dx;
		if(moveableY(player_y, player_dy))
			player_y += player_speed_y * player_dy;
		
		player.setPlayer_x(player_x);
		player.setPlayer_y(player_y);
		player.setPlayer_speed_x(player_speed_x);
		player.setPlayer_speed_y(player_speed_y);
	}
	
	// Moveable within x-direction
	private boolean moveableX(int x, int dx)
	{
		if(dx > 0)
			x += entity_width;
		
		int player_speed_x = player.getPlayer_speed_x();
		// Left
		if(dx < 0 && 0 < (x - player_speed_x))
			return true;
		// Right
		else if(dx > 0 && (x + player_speed_x) < this.width)
			return true;
		
		return false;
	}

	// Moveable within y-direction
	private boolean moveableY(int y, int dy)
	{		
		if(dy > 0)
			y += entity_height;
		
		int player_speed_y = player.getPlayer_speed_y();
		// Up
		if(dy < 0 && 0 < (y - player_speed_y))
			return true;
		// Down
		else if(dy > 0 && (y + player_speed_y) < this.height)
			return true;
		
		return false;
	}
	
	private void updateNPC()
	{
		int npc_x = npc.getEnemy_x();
		int npc_y = npc.getEnemy_y();
		
		int npc_speed_x = npc.getSpeed_x();
		int npc_speed_y = npc.getSpeed_y();
		
		// Left
		if(npc_x < 0)
			npc_speed_x = 1;
		// Right
		else if(this.width < (npc_x + entity_width))
			npc_speed_x = -1;
		// Up
		if(npc_y < 0)
			npc_speed_y = 1;
		// Down
		else if(this.height < (npc_y + entity_height))
			npc_speed_y = -1;
		
		npc_x += npc_speed_x * entity_max_speed;
		npc_y += npc_speed_y * entity_max_speed;
		
		// Randomize dirt-generation
		int dirt_rng = (int) (Math.random() * RNG);
		if(dirt_rng == 0 && !dirt_placed)
		{
			dirt_placed = true;
			dirt_x = npc_x;
			dirt_y = npc_y + (entity_height / 2);
		}
		
		npc.setEnemy_x(npc_x);
		npc.setEnemy_y(npc_y);
		npc.setSpeed_x(npc_speed_x);
		npc.setSpeed_y(npc_speed_y);
	}
	
	// Check if player can 'clean' dirt, a.k.a. if player intersects majority of dirt
	private void updateCleaning()
	{
		boolean valid_x = false;
		boolean valid_y = false;
	
		int player_x = player.getPlayer_x();
		int player_y = player.getPlayer_y();
		
		// If majority of dirt-width within player-width
		if(Math.abs(player_x - dirt_x) <= (0.2 * (double) entity_width))
			valid_x = true;
		
		// If dirt-height completely within player-height
		if(player_y < dirt_y && (dirt_y + dirt_heigth) < (player_y + entity_height))
			valid_y = true;
		
		// Player covered enough dirt a.k.a. clean
		if(valid_x && valid_y)
		{
			points++;
			dirt_placed = false;
			System.out.println(points);
		}
	}

	@Override
	public void paintComponent(Graphics g_1d)
	{
		super.paintComponent(g_1d);
		
		Graphics2D g_2d = (Graphics2D) g_1d;
		
		// Paint player
		g_2d.drawImage(player_img, player.getPlayer_x(), player.getPlayer_y(), entity_width, entity_height, null);
		
		// Paint dirt if it is generated
		if(dirt_placed)
			g_2d.drawImage(dirt_img, dirt_x, dirt_y, dirt_width, dirt_heigth, null);

		// Paint NPC
		g_2d.drawImage(npc_img, npc.getEnemy_x(), npc.getEnemy_y(), entity_width, entity_height, null);
		
		g_2d.dispose();
	}
}
