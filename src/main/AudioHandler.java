package main;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioHandler implements LineListener
{
//	String file_name = "src/audio_files/";
	String file_name = "src/audio_files/short_ver/";
	boolean repeat;
	boolean audio_finished = false;
	
	Clip clip;
	FloatControl float_ctrl;
//	CountDownLatch playingFinished;
	
	int current_audio_index;
//	boolean user_stop = false;
	
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
		
		// Play audio file
		try
		{
			clip = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
			clip.open(AudioSystem.getAudioInputStream(file));
			clip.addLineListener(this);
			float_ctrl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			float_ctrl.setValue(0);
			clip.start();
			
			audio_finished = false;
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
	}
	
	@Override
	public void update(LineEvent event)
	{
		// Audio file finished
		if(event.getType() == LineEvent.Type.STOP)
		{				
			clip.close();
			/*
			if(repeat && !user_stop)
				new AudioHandler("", true, current_song_index);
			*/
//			if(repeat)

			audio_finished = true;
		}
	}

	public boolean isAudio_finished()
	{
		return audio_finished;
	}

	public int getCurrent_audio_index()
	{
		return current_audio_index;
	}
	
	// Randomize next song
	private String getRNGSong(int old_song_index)
	{
		String[] song_names = {"ram_ranch_46.wav",
							   "vada_a_borde_cazzo-remix.wav",
							   "canelloni_macaroni.wav",
							   "caramelldansen.wav",
							   "italian_sfx.wav",
							   "miss_li.wav"};
		
		double[][] nums = new double[6][2];
		nums[0][0]	= 1;  // Ram Ranch			 - 2%
		nums[1][0]	= 4;  // Vada a bordo, Cazzo - 8%
		nums[2][0]	= 10; // Canelloni Macaroni	 - 20%
		nums[3][0]	= 10; // Caramelldansen		 - 20%
		nums[4][0]	= 10; // Italian SFX		 - 20%
		nums[5][0]	= 14; // Miss Li			 - 29%

		nums[0][1] = 1;
		for(int i = 1; i < nums.length; i++)
			nums[i][1] = nums[i][0] + nums[i - 1][1];
		
		double max = nums[nums.length - 1][1];
		
		int new_song_index = old_song_index;
		int i = 0;
		
		// Ensure new song is not the same as the previous one
		while(new_song_index == old_song_index)
		{
			double RNG = Math.random() * max;
			
			// First array entry
			if(RNG < nums[i][1])
				new_song_index = i;
			
			// Rest of entries
			for(i = 1; i < nums.length; i++)
			{
				if(nums[i - 1][1] <= RNG && RNG < nums[i][1])
				{
					new_song_index = i;
					break;
				}
			}
			
			i = 0;
		}
		
		current_audio_index = new_song_index;
		return song_names[current_audio_index];
	}

	public void endCurrentSong()
	{
//		user_stop = true;
		clip.stop();
	}
	
	public void raiseVolume(int level)
	{		
		float_ctrl.setValue(level);
	}
	public void lowerVolume()
	{
		float_ctrl.setValue(-10);
	}
}