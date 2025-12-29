package mini_games;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import main.GamePanel;
import main.GameTimer;
import main.KeyHandler;

public class Albin extends JPanel
{
	int width, height;
	BufferedImage background_img, car_left, car_right;
	int time_1, time_2;
	TimerTask task1, task2;
	int car_dim;
	int car_x, car_y;
	char car_dir = 'r';	// <left | right>
	int dx_1, dx_2;
	
	// Arguments
	JFrame frame;
	JLabel top;
	GameTimer game_timer;
	KeyHandler key_handler;
	int player_x_passing;
	int player_y_passing;
	
	public Albin(JFrame frame, JLabel top, GameTimer game_timer, KeyHandler key_handler, int player_x, int player_y)
	{
		super();
			this.width  = frame.getWidth();
			this.height = 9 * (frame.getHeight() / 10);
		setPreferredSize(new Dimension(this.width, this.height));
		setLocation(0, 0);
		setFocusable(false);
		setBackground(new Color(0, 64, 0));
		
		try
		{
			background_img	= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/albin/background_img.png"));
			car_left		= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/albin/volvo_tot_LEFT-transp.png"));
			car_right		= ImageIO.read(getClass().getResourceAsStream("/image_files/minigame_imgs/albin/volvo_tot_RIGHT-transp.png"));
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		
		this.frame				= frame;
		this.top				= top;
		this.game_timer			= game_timer;
		this.key_handler		= key_handler;
		this.player_x_passing	= player_x;
		this.player_y_passing	= player_y;
		
		// Dimension and y-pos.
		car_dim = width / 5;
		car_y = (height - car_dim) / 2;
		
		// Randomize how many seconds mini-game will take, range: [3, 8] per "cycle"
		int max = 8;
		int min = 3;
		int game_time = timeStrToNum(game_timer.getTime_str());
//		if(game_time < 2 * max)
//			max = game_time / 2;

		time_1 = (int) ((Math.random() * (max - min)) + min);	// Cycle 1
			time_1 *= 1000;
		time_2 = (int) ((Math.random() * (max - min)) + min);	// Cycle 2
			time_2 *= 1000;
		
		Timer timer_1 = new Timer();
		Timer timer_2 = new Timer();
		
		frame.add(this);
		frame.repaint();

		// Right-ward step size
		dx_1 = width / time_1;
		if(dx_1 <= 0)
			dx_1 = 1;
		
		// Left-ward step size
		dx_2 = width / time_2;
		if(dx_2 <= 0)
			dx_2 = 1;
		
		task1 = new TimerTask()
		{
			int mili_sec = 0;

			@Override
			public void run()
			{
				// Cycle 1 passed
				if(mili_sec >= time_1)
				{
					timer_1.cancel();
					car_dir = 'l';
					car_x = width;
					timer_2.scheduleAtFixedRate(task2, 0, 1);
				}
				
				car_x += dx_1;
				mili_sec++;
				repaint();
			}
		};

		task2 = new TimerTask()
		{
			int mili_sec = 0;
			
			@Override
			public void run()
			{
				// Cycle 2 passed
				if(mili_sec >= time_2)
				{
					timer_2.cancel();
					killClass();
				}				
				
				car_x -= dx_2;
				mili_sec++;
				repaint();
			}

		};
		
		timer_1.scheduleAtFixedRate(task1, 0, 1);
	}

	// Get gaming time as int
	private int timeStrToNum(String time_str)
	{
		String HH_str		= time_str.substring(0, 2);
		String MM_str		= time_str.substring(3, 5);
		String SS_str		= time_str.substring(6, 8);
		String sub_SS_str	= time_str.substring(9, 11);
		
		int HH		= Integer.valueOf(HH_str);
		int MM		= Integer.valueOf(MM_str);
		int SS		= Integer.valueOf(SS_str);
		int sub_SS	= Integer.valueOf(sub_SS_str);
		SS += sub_SS / 100;
		
		return HH + MM + SS;
	}
	
	// Remove local GUI and gen. main GamePanel
	private void killClass()
	{
		game_timer.setTime_coeff(1);
		frame.remove(this);
		new GamePanel(frame, top, game_timer, key_handler, player_x_passing, player_y_passing);
	}

	@Override
	public void paintComponent(Graphics g_1d)
	{
		super.paintComponent(g_1d);	
		Graphics2D g_2d = (Graphics2D) g_1d;
		
		// Draw background
		g_2d.drawImage(background_img, 0, 0, this.width, this.height, null);
		
		// Draw car
		switch(car_dir)
		{
			// Left-ward
			case 'l':			
				g_2d.drawImage(car_left, car_x, car_y, (int) (1.5 * car_dim), car_dim, null);
				break;
			// Right-ward
			case 'r':
				g_2d.drawImage(car_right, car_x, car_y, (int) (1.5 * car_dim), car_dim, null);
				break;
		}
		
		g_2d.dispose();
	}
}
