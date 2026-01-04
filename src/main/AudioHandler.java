package main;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioHandler implements LineListener
{
	String file_name = "src/audio_files/";
	boolean repeat;
	Clip clip;
	int current_song_index;
	boolean user_stop = false;
	
	public AudioHandler(String file_name, boolean repeat, int current_song_index)
	{
		if(file_name.equals(""))
			this.file_name += getRNGSong(current_song_index);
		else
			this.file_name += file_name;
		this.repeat = repeat;
		
		File file = new File(this.file_name);
		if(file.exists())
			System.out.println();
		clip = null;
		
		try
		{
			clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
			clip.addLineListener(this);
			clip.open(AudioSystem.getAudioInputStream(file)); 
		} catch (LineUnavailableException lue)
		{
			lue.printStackTrace();
		} catch(IOException ioe)
		{
			ioe.printStackTrace();
		} catch(UnsupportedAudioFileException uafe)
		{
			uafe.printStackTrace();
		}
		
		clip.start();
	}
	
	@Override
	public void update(LineEvent event)
	{
		{
			/*
			if(repeat)
				new AudioHandler("", true, current_song_index);
			else if(!repeat)
				clip.close();
			*/
			if(event.getType() == LineEvent.Type.STOP)
			{				
				clip.close();
				if(repeat && !user_stop)
					new AudioHandler("", true, current_song_index);
			}
		}
	}

	public int getCurrent_song_index()
	{
		return current_song_index;
	}
	
	private String getRNGSong(int old_song_index)
	{
		String[] song_names = {"ram_ranch_46.wav",
							   "canelloni_macaroni.wav",
							   "caramelldansen.wav",
							   "italian_sfx.wav",
							   "miss_li.wav"};
		
		double[][] nums = new double[5][2];
		nums[0][0]	= 1; // Ram Ranch			- 7%
		nums[1][0]	= 3; // Canelloni Macaroni	- 21%
		nums[2][0]	= 3; // Caramelldansen		- 21%
		nums[3][0]	= 3; // Italian SFX			- 21%
		nums[4][0]	= 4; // Miss Li				- 29%
		
		nums[0][1] = 1;
		for(int i = 1; i < nums.length; i++)
			nums[i][1] = nums[i][0] + nums[i - 1][1];
		
		double max = nums[nums.length - 1][1];
		
		int new_song_index = old_song_index;
		int i = 0;
		
		// Ensure new song starts playing
		while(new_song_index == old_song_index)
		{
			double RNG = Math.random() * max;
			
//			if(RNG < nums[i][1])
//				old_song_index = i;	
			for(i = 1; i < nums.length; i++)
			{
				if(nums[i - 1][1] <= RNG && RNG < nums[i][1])
				{
					new_song_index = i;
					break;
				}
			}
		}
		
		current_song_index = new_song_index;
		return song_names[new_song_index];
	}

	public void endCurrentSong()
	{
		user_stop = true;
		clip.close();
	}
	/*
	public String getCurrentSongStr()
	{
		String[] song_names = {"ram_ranch_46.wav",
				   "canelloni_macaroni.wav",
				   "caramelldansen.wav",
				   "italian_sfx.wav",
				   "miss_li.wav"};
		String current_song_name = song_names[current_song_index];
		current_song_name = current_song_name.
	
		return song_names[current_song_index];
	}
	*/
}