package menu;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import main.AudioHandler;

public class StartMenu extends JPanel
{
	int width, height;
	
	Color blue = new Color(0, 162, 232);
	Color yellow = new Color(239, 228, 176);
	
	Font font = new Font("Arial", Font.PLAIN, 30);
	
	public StartMenu(JFrame frame, JLabel top, AudioHandler game_audio)
	{
		super();
			this.width  = frame.getWidth();
			this.height = (9 * frame.getHeight()) / 10;
		setPreferredSize(new Dimension(this.width, this.height));
		setLocation(0, 0);
		setBackground(blue);
		setLayout(new GridLayout(1, 3));
		frame.add(this);
			JPanel filler_left = new JPanel();
			filler_left.setBackground(blue);
			add(filler_left);
			
			// Container of user optns.
			JPanel optn_container = new JPanel();
			optn_container.setBackground(yellow);
			optn_container.setLayout(new GridLayout(6, 1));
			add(optn_container);
				JPanel filler_top = new JPanel();
				filler_top.setBackground(blue);
				optn_container.add(filler_top);
				
				MenuButton play_btn = new MenuButton("Play", 4, 4, 2, 4);
				play_btn.setFrame(frame);
				play_btn.setTop(top);
				play_btn.setPanel(this);
				play_btn.setAudioHandler(game_audio);
				optn_container.add(play_btn);
				
/*				JPanel music_optn = new JPanel();
				music_optn.setLayout(new GridLayout(1, 2));
				optn_container.add(music_optn);
					// Name of current song
					String current_song_name = game_audio.getCurrentSongStr();
					JLabel current_song = new JLabel(current_song_name, JLabel.CENTER);
					current_song.setBackground(blue);
					current_song.setForeground(yellow);
					current_song.setFont(font);
					current_song.setBorder(BorderFactory.createMatteBorder(4, 4, 4, 4, yellow));
					current_song.setOpaque(true);
					music_optn.add(current_song);
					*/
						
					// Ability to change song, (randomize new)
				MenuButton switch_song_btn = new MenuButton("Switch Song", 2, 4, 2, 4);
//				switch_song_btn.setText("<html><p> Switch </p><p> Song </p></htlm>");
				switch_song_btn.setFrame(frame);
				switch_song_btn.setTop(top);
				switch_song_btn.setPanel(this);
				switch_song_btn.setAudioHandler(game_audio);
				optn_container.add(switch_song_btn);
				
				MenuButton leaderboard_btn = new MenuButton("Leaderboard", 2, 4, 2, 4);
				leaderboard_btn.setFrame(frame);
				leaderboard_btn.setTop(top);
				leaderboard_btn.setPanel(this);
				leaderboard_btn.setAudioHandler(game_audio);
				optn_container.add(leaderboard_btn);
				
				MenuButton exit_btn = new MenuButton("Exit", 2, 4, 4, 4);
				optn_container.add(exit_btn);
				
				JPanel filler_bottom = new JPanel();
				filler_bottom.setBackground(blue);
				optn_container.add(filler_bottom);
			JPanel filler_right = new JPanel();
			filler_right.setLayout(new GridLayout(3, 1));
			add(filler_right);
				JPanel filler_top_right = new JPanel();
				filler_top_right.setBackground(blue);
				filler_right.add(filler_top_right);

				JPanel filler_top_left = new JPanel();
				filler_top_left.setBackground(blue);
				filler_right.add(filler_top_left);
				
				JLabel esc_info_text = new JLabel("<html><p>Press Esc. to exit the game</p><p>and return to the start menu!</p></html>", JLabel.CENTER);
				esc_info_text.setBackground(blue);
				esc_info_text.setForeground(yellow);
				esc_info_text.setFont(font);
				esc_info_text.setOpaque(true);
				filler_right.add(esc_info_text);
		frame.setVisible(true);
	}
}