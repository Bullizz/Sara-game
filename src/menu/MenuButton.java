package menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.border.Border;

import main.AudioHandler;
import main.GamePanel;
import main.KeyHandler;

public class MenuButton extends JButton implements /*ActionListener, */MouseListener
{
	Color blue = new Color(0, 162, 232);
	Color yellow = new Color(239, 228, 176);
	
	String btn_name;
	
	Border border;
	Border hover_border;
	
	Font font = new Font("Arial", Font.PLAIN, 40);
	Font hover_font = new Font("Arial", Font.BOLD, 40);
	
	public MenuButton(String btn_name, int top, int left, int bot, int right)
	{
		super(btn_name);
		
		this.btn_name	= btn_name;
		border			= BorderFactory.createMatteBorder(top, left, bot, right, yellow);
		hover_border	= BorderFactory.createMatteBorder(2 * top, 2 * left, 2 * bot, 2 * right, yellow);
		
		new JButton();
		setBackground(blue);
		setForeground(yellow);
		setBorder(border);
		setFont(font);
		addMouseListener(this);
	}

	JFrame frame;
	JLabel top;
	JComponent current_panel;
	AudioHandler game_audio;
	
	@Override
	public void mouseClicked(MouseEvent e){}
	@Override
	public void mousePressed(MouseEvent press)
	{
		switch(btn_name)
		{
			case "Play":
				frame.remove(current_panel);
				KeyHandler key_handler = new KeyHandler();
				frame.addKeyListener(key_handler);
				frame.requestFocus();
				new GamePanel(frame, top, null, key_handler, game_audio, 972, 101);
				break;
			case "Main Menu":
				frame.remove(current_panel);
				top.setText("Vada a Bordo, Cazzo!");
				new StartMenu(frame, top, game_audio);
				break;
			case "Switch Song":
				int current_song_index = game_audio.getCurrent_song_index();
				game_audio.endCurrentSong();
				game_audio = new AudioHandler("", true, current_song_index);
				frame.remove(current_panel);
				new StartMenu(frame, top, game_audio);
				break;
			case "Leaderboard":
				frame.remove(current_panel);
				new EndMenu(frame, top, game_audio, "", top.getText());
				break;
			case "Clear Leaderboard":
				clearLeaderboard();
				frame.remove(current_panel);
				new EndMenu(frame, top, game_audio, "", top.getText());
				break;
			case "Exit":
				System.exit(0);
				break;
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e){}
	@Override
	public void mouseEntered(MouseEvent enter)
	{
		setFont(hover_font);
		setBorder(hover_border);
	}
	@Override
	public void mouseExited(MouseEvent exit)
	{		
		setFont(font);
		setBorder(border);
	}

	public void setFrame(JFrame frame)
	{
		this.frame = frame;
	}
	public void setTop(JLabel top)
	{
		this.top = top;
	}
	public void setPanel(JComponent start_menu)
	{
		current_panel = start_menu;
	}
	public void setAudioHandler(AudioHandler game_audio)
	{
		this.game_audio = game_audio;
	}

	// Remove leaderboard file and gen. new empty
	private void clearLeaderboard()
	{
		File file = new File("leaderboard.txt");
		try
		{
			if(file.delete())
				file.createNewFile();
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}