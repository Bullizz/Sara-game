package mini_games;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import handlers.AudioHandler;
import handlers.KeyHandler;

import main.ErrorManagement;
import main.GamePanel;
import main.GameTimer;

import menu.StartMenu;

public class Albin extends JPanel
{
	/**/
	private static final long serialVersionUID = 6L;
	
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
			this.height = (9 * frame.getHeight()) / 10;
		setPreferredSize(new Dimension(this.width, this.height));
		setLocation(0, 0);
		setFocusable(false);
		
		try
		{
			background_img	= ImageIO.read(getClass().getResourceAsStream("/image_files/Albin/background_img.png"));
			car_left		= ImageIO.read(getClass().getResourceAsStream("/image_files/Albin/volvo_LEFT.png"));
			car_right		= ImageIO.read(getClass().getResourceAsStream("/image_files/Albin/volvo_RIGHT.png"));
		} catch(IOException e)
		{
			new ErrorManagement("<html><p>mini_games.Albin:</p><p>Reading File Error</p></html>", ioe.toString());
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
		// range: [3, 8] seconds per "cycle"
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
				AudioHandler game_audio = getGame_audio();
				if(game_audio.isAudio_finished())
				{
					int current_audio_index = game_audio.getCurrent_audio_index();
					game_audio = new AudioHandler("", current_audio_index);
					game_audio.lowerVolume();
					setGame_audio(game_audio);
				}
				
				// Cycle 1 done
				if(car_x > width || key_handler.GamePanel_esc_pressed)
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
				AudioHandler game_audio = getGame_audio();
				if(game_audio.isAudio_finished())
				{
					int current_audio_index = game_audio.getCurrent_audio_index();
					game_audio = new AudioHandler("", current_audio_index);
					game_audio.lowerVolume();
					setGame_audio(game_audio);
				}
				
				// Cycle 2 done
				if(car_x + car_width < 0 || key_handler.GamePanel_esc_pressed)
				{
					timer_2.cancel();
					albin_audio.endCurrentSong();
					game_audio.raiseVolume(0);
					killClass();
				}
				
				car_x--;
				repaint();
			}
		};
		
		game_audio.lowerVolume();
		albin_audio = new AudioHandler("sfx/car.wav", -1);
		timer_1.scheduleAtFixedRate(task1, 0, time_1 / width);
	}

	public AudioHandler getGame_audio()
	{
		return game_audio;
	}
	public void setGame_audio(AudioHandler game_audio)
	{
		this.game_audio = game_audio;
	}
	
	// Remove local GUI-comps. and gen. GamePanel or StartMenu
	private void killClass()
	{
		game_timer.setTime_coeff(1);
		frame.remove(this);
		if(key_handler.GamePanel_esc_pressed)
		{
			game_timer.timer.cancel();
			
			frame.removeKeyListener(key_handler);
			frame.remove(this);

			top.setText("Vada a Bordo, Cazzo!");
			
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