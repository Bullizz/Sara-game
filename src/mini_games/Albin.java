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

import main.AudioHandler;
import main.GamePanel;
import main.GameTimer;
import main.KeyHandler;
import menu.StartMenu;

public class Albin extends JPanel
{
	int width, height;
	BufferedImage background_img;
	int time_1, time_2;
	TimerTask task1, task2;
	
	// Arguments
	JFrame frame;
	JLabel top;
	GameTimer game_timer;
	KeyHandler key_handler;
	AudioHandler game_audio;
	int player_x_passing;
	int player_y_passing;

	// Car parameters
	int car_width 	= 576;
	int car_height 	= 384;
	char car_dir 	= 'r';	// <'l'|'r'> = <left|right>
	int car_x, car_y;
	BufferedImage car_left, car_right;
	
	AudioHandler albin_audio;

	public Albin(JFrame frame, JLabel top, GameTimer game_timer, KeyHandler key_handler, AudioHandler game_audio, int player_x, int player_y)
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
		this.game_audio			= game_audio;
		this.player_x_passing	= player_x;
		this.player_y_passing	= player_y;
		
		// Dimension and y-pos.
		car_y = (height - car_height) / 2;
		
		// Randomize how many seconds mini-game will take,
		// range: [3, 8] per "cycle"
		int max = 8;
		int min = 3;
		
		time_1 = (int) ((Math.random() * (max - min)) + min);	// Cycle 1 length [s]
			time_1 *= 1000;
		time_2 = (int) ((Math.random() * (max - min)) + min);	// Cycle 2 length [s]
			time_2 *= 1000;
		
		Timer timer_1 = new Timer();
		Timer timer_2 = new Timer();
		
		frame.add(this);
		frame.repaint();
		
		
		// Timer for cycle 1 (left --> right)
		task1 = new TimerTask()
		{
			@Override
			public void run()
			{
				// Cycle 1 done
				if(car_x > width || key_handler.GamePanel_space_pressed)
				{
					timer_1.cancel();
					car_dir = 'l';
					timer_2.scheduleAtFixedRate(task2, 0, time_2 / width);
				}
				
				car_x++;
				repaint();
			}
		};

		// Timer for cycle 2 (left <-- right)
		task2 = new TimerTask()
		{
			@Override
			public void run()
			{
				// Cycle 2 done
				if(car_x + car_width < 0 || key_handler.GamePanel_space_pressed)
				{
					timer_2.cancel();
					try
					{
						albin_audio.endCurrentSong();
					} catch(Exception e){}
					game_audio.raiseVolume(0);
					killClass();
				}
				
				car_x--;
				repaint();
			}
		};
		
		game_audio.lowerVolume();
		albin_audio = new AudioHandler("sfx/car.wav", false, -1);
		timer_1.scheduleAtFixedRate(task1, 0, time_1 / width);
	}
	
	// Remove local GUI-comps. and gen. main GamePanel or StartMenu
	private void killClass()
	{
		game_timer.setTime_coeff(1);
		frame.remove(this);
		if(key_handler.GamePanel_space_pressed)
		{
			game_timer.timer.cancel();
			
			frame.removeKeyListener(key_handler);
			frame.remove(this);

			top.setText("Vada a Bordo, Cazzo!");
			albin_audio.endCurrentSong();
			
			new StartMenu(frame, top, game_audio);
		}
		else
			new GamePanel(frame, top, game_timer, key_handler, game_audio, player_x_passing, player_y_passing);
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
				g_2d.drawImage(car_left, car_x, car_y, car_width, car_height, null);
				break;
			// Right-ward
			case 'r':
				g_2d.drawImage(car_right, car_x, car_y, car_width, car_height, null);
				break;
		}
		
		g_2d.dispose();
	}
}