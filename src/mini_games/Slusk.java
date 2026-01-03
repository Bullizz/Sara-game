package mini_games;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import main.GamePanel;
import main.GameTimer;
import main.KeyHandler;
import menu.StartMenu;

public class Slusk extends JPanel implements Runnable
{
	int width, height;
	Thread slusk_thread;
	
	boolean game_loop_running;
	BufferedImage background_img;
	int points_1, points_2, points_3, points_4, points_5, points_MAX;
	BufferedImage big_pic;
	int big_x, big_width, big_height;
	
	// Arguments
	JFrame frame;
	JLabel top;
	GameTimer game_timer;
	KeyHandler key_handler;
	int player_x_passing;
	int player_y_passing;
	
	// Liquid parameters	
	boolean paint_liquid_1 = true;
	boolean paint_liquid_2 = true;
	boolean paint_liquid_3 = true;
	boolean paint_liquid_4 = true;
	boolean paint_liquid_5 = true;
	
	int liquid_width  = 58;
	int liquid_height = 97; // = bong_height / 5, bong-tratt height, liquid-1, liquid-2, liquid-3, and liquid-4
	int liquid_x	  = 930;

	// Space sign parameters
	BufferedImage space_img_active, space_img_inactive;
	boolean space_sign 	= false;
	int space_width		= 320;
	int space_height	= 138;
	int space_x 	 	= 251;
	int space_y 	 	= 319;
	
	public Slusk(JFrame frame, JLabel top, GameTimer game_timer, KeyHandler key_handler, int player_x, int player_y)
	{
		super();
			this.width = frame.getWidth();
			this.height = 9 * (frame.getHeight() / 10);
		setPreferredSize(new Dimension(width, height));
		setLocation(0, 0);
		
		try
		{
//			background_img 		= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/slusk/background_img.png"));
			big_pic 			= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/slusk/big_pic-transp.png"));
			space_img_active	= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/slusk/space_bar_2-transp.png"));
			space_img_inactive	= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/slusk/space_bar-transp.png"));
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		
		assignPoints();
		
		this.frame				= frame;
		this.top				= top;
		this.game_timer			= game_timer;
		this.key_handler		= key_handler;
		this.player_x_passing	= player_x;
		this.player_y_passing	= player_y;
		
		key_handler.setSlusk_active(true);
		key_handler.setSlusk_points(0);
		
		big_x		= width / 3;
		big_width 	= width / 3;
		big_height 	= height;
		
		frame.add(this);
		frame.repaint();
		
		initSluskThread();
	}

	private void initSluskThread()
	{
		game_loop_running = true;
		
		slusk_thread = new Thread(this);
		slusk_thread.start();
	}

	int FPS = 60;
	@Override
	public void run()
	{
		// Master game-loop
		while(slusk_thread != null)
		{
			double draw_interval = Math.pow(10, 9);
			draw_interval /= FPS;
			double delta = 0;
			long last_time = System.nanoTime();
			long current_time;
			
			int time_counter = 0;
			// Slave game-loop
			while(game_loop_running)
			{
				current_time = System.nanoTime();
				delta += (current_time - last_time) / draw_interval;
				last_time = current_time;
				if(delta >= 1)
				{
					int current_points = key_handler.getSlusk_points();
					
					// Enough points to 'clear' a liquid level
					if(paint_liquid_1 && current_points >= points_1)
						paint_liquid_1 = false;
					if(paint_liquid_2 && current_points >= points_1 + points_2)
						paint_liquid_2 = false;
					if(paint_liquid_3 && current_points >= points_1 + points_2 + points_3)
						paint_liquid_3 = false;
					if(paint_liquid_4 && current_points >= points_1 + points_2 + points_3 + points_4)
						paint_liquid_4 = false;
					if(paint_liquid_5 && current_points >= points_MAX)
						paint_liquid_5 = false;
					
					repaint();
					
					// Minigame completed
					if(current_points > points_MAX || key_handler.GamePanel_space_pressed)
					{
						key_handler.setSlusk_active(false);
						game_loop_running = false;
						slusk_thread = null;
						frame.remove(this);
					}
					
					time_counter++;
					// Every 30 frame, i.e. every half second
					if(time_counter % 30 == 0)
					{
						toggleSpaceSign();
						time_counter = 0;
					}
					
					delta = 0;
				}
			} // End  of slave game-loop
		} // End of master game-loop
		if(key_handler.GamePanel_space_pressed)
		{
			game_timer.timer.cancel();
			
			frame.removeKeyListener(key_handler);
			frame.remove(this);

			top.setText("Vada a Bordo, Cazzo!");
			
			new StartMenu(frame, top);
		}
		else
			new GamePanel(frame, top, game_timer, key_handler, player_x_passing, player_y_passing);
	}
	
	// Randomize value to be assigned to points
	private void assignPoints()
	{
		int min = 5;
		int max = 15;
		
		points_1 = (int) ((Math.random() * (max - min)) + min);
		points_2 = (int) ((Math.random() * (max - min)) + min);
		points_3 = (int) ((Math.random() * (max - min)) + min);
		points_4 = (int) ((Math.random() * (max - min)) + min);
		points_5 = (int) ((Math.random() * (max - min)) + min);
		points_MAX = points_1 + points_2 + points_3 + points_4 + points_5;
	}
	
	// Toggle between true & false
	private void toggleSpaceSign()
	{
		if(space_sign)
			space_sign = false;
		else if(!space_sign)
			space_sign = true;
	}
	
	@Override
	public void paintComponent(Graphics g_1d)
	{
		super.paintComponent(g_1d);
		Graphics2D g_2d = (Graphics2D) g_1d;
		
		// Paint background
		g_2d.setColor(Color.CYAN);
		g_2d.fillRect(0, 0, width, height);
		
		// Paint space sign
		g_2d.rotate(Math.toRadians(-45), space_x + (space_width / 2), space_y + (space_height / 2));
		if(space_sign)
			g_2d.drawImage(space_img_active, space_x, space_y, space_width, space_height, null);
		else if(!space_sign)
			g_2d.drawImage(space_img_inactive, space_x, space_y, space_width, space_height, null);
		
		// Undo rotation
		g_2d.rotate(Math.toRadians(45), space_x + (space_width / 2), space_y + (space_height / 2));
		
		// Paint liquid
		g_2d.setColor(Color.ORANGE);
		int liquid_y = liquid_height;
		
		if(paint_liquid_1)
			g_2d.fillRect(liquid_x, liquid_y, liquid_width, liquid_height);
		liquid_y += liquid_height;
		
		if(paint_liquid_2)
			g_2d.fillRect(liquid_x, liquid_y, liquid_width, liquid_height);
		liquid_y += liquid_height;
		
		if(paint_liquid_3)
			g_2d.fillRect(liquid_x, liquid_y, liquid_width, liquid_height);
		liquid_y += liquid_height;
		
		if(paint_liquid_4)
			g_2d.fillRect(liquid_x, liquid_y, liquid_width, liquid_height);
		liquid_y = (height / 2) - liquid_width;
		
		if(paint_liquid_5)
			g_2d.fillRect(liquid_x + liquid_width, liquid_y, liquid_height, liquid_width);
		
		// Paint big-pic
		g_2d.drawImage(big_pic, big_x, 0, big_width, big_height, null);
		
		g_2d.dispose();
	}
}