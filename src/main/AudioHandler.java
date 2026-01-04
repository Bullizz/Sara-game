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
//	C:\Users\albin\eclipse-workspace\Sara_Game\src\audio_files\italian_sfx.wav

	String file_name = "src/audio_files/";
	boolean repeat;
	Clip clip;
	
	public AudioHandler(String file_name, boolean repeat)
	{
		if(file_name.equals(""))
			this.file_name += getRNGSong();
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
		} catch (LineUnavailableException e)
		{
			e.printStackTrace();
		} catch(IOException e)
		{
			e.printStackTrace();
		} catch(UnsupportedAudioFileException e)
		{
			e.printStackTrace();
		}
		
		clip.start();
	}
	
	@Override
	public void update(LineEvent event)
	{
		if(event.getType() == LineEvent.Type.STOP)
		{
			if(repeat)
			{
				new AudioHandler("", true);
			}
			else if(!repeat)
			{
				clip.close();
			}
		}
	}
	
	private String getRNGSong()
	{
		String[] song_names = {"ram_ranch_46.wav",
							   "canelloni_macaroni.wav",
							   "caramelldansen.wav",
							   "italian_sfx.wav",
							   "miss_li.wav"
							   };
		
		double[][] nums = new double[5][2];
		nums[0][0]	= 1; // Ram Ranch - 7%
		nums[1][0]	= 3; // Canelloni Macaroni - 21%
		nums[2][0]	= 3; // Caramelldansen - 21%
		nums[3][0]	= 3; // Italian SFX - 21%
		nums[4][0]	= 4; // Miss Li - 29%
		
		nums[0][1] = 1;
		for(int i = 1; i < nums.length; i++)
			nums[i][1] = nums[i][0] + nums[i - 1][1];
		
		double max = nums[nums.length - 1][1];
		double RNG = Math.random() * max;
		
		int i = 0;
		if(RNG < nums[i][1])
			return song_names[i];	
		
		for(i = 1; i < nums.length; i++)
		{
			if(nums[i - 1][1] <= RNG && RNG < nums[i][1])
				break;
		}
		return song_names[i];
	}

	public void endCurrentSong()
	{
		clip.close();
	}
}