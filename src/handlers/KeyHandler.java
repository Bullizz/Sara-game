package handlers;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener
{
	public boolean GamePanel_esc_pressed = false;
	public boolean UP, LEFT, DOWN, RIGHT;
	int[] direction_arr = {0, 0};
	
	// Slusk minigame -variables
	int slusk_points;
	boolean slusk_space_pressed;
	boolean slusk_active;
	
	// Attila minigame -variables
	boolean attila_active;
	boolean key_available;
	char current_key;
	
	@Override
	public void keyTyped(KeyEvent e){} // Unused
	@Override
	public void keyPressed(KeyEvent press)
	{
		int user_inp = press.getKeyCode();
		int dx = getDirection_arr()[0];
		int dy = getDirection_arr()[1];
		
		if(user_inp == KeyEvent.VK_ESCAPE)
			GamePanel_esc_pressed = true;
		
		// For slusk minigame
		if(slusk_active)
		{
			if(user_inp == KeyEvent.VK_SPACE && !slusk_space_pressed)
			{
				slusk_space_pressed = true;
				slusk_points++;
			}
		}

		// For attila minigame
		else if(attila_active && key_available)
		{
			switch(user_inp)
			{
				case KeyEvent.VK_W:
					current_key = 'w';
					break;
				case KeyEvent.VK_A:
					current_key = 'a';
					break;
				case KeyEvent.VK_S:
					current_key = 's';
					break;
				case KeyEvent.VK_D:
					current_key = 'd';
					break;
			}
		}
		
		else
		{
			if((user_inp == KeyEvent.VK_UP || user_inp == KeyEvent.VK_W))// && !DOWN)
			{
				UP = true;
				dy = -1;
			}
			if((user_inp == KeyEvent.VK_LEFT || user_inp == KeyEvent.VK_A))// && !RIGHT)
			{
				LEFT = true;
				dx = -1;
			}
			if((user_inp == KeyEvent.VK_DOWN || user_inp == KeyEvent.VK_S))// && !UP)
			{
				DOWN = true;
				dy = 1;
			}
			if((user_inp == KeyEvent.VK_RIGHT || user_inp == KeyEvent.VK_D))// && !LEFT)
			{
				RIGHT = true;
				dx = 1;
			}
			
			setDirection_arr(new int[]{dx, dy});
		}
	}
	
	@Override
	public void keyReleased(KeyEvent release)
	{
		int user_inp = release.getKeyCode();
		int dx = getDirection_arr()[0];
		int dy = getDirection_arr()[1];
		
		if(slusk_active)
		{			
			if(release.getKeyCode() == KeyEvent.VK_SPACE && slusk_space_pressed)
				slusk_space_pressed = false;
		}
		
		else if(attila_active)
		{
			if(current_key == 'w' && release.getKeyCode() == KeyEvent.VK_W)
			{
				current_key = '\0';
				key_available = true;						
			}
			if(current_key == 'a' && release.getKeyCode() == KeyEvent.VK_A)
			{
				current_key = '\0';
				key_available = true;						
			}
			if(current_key == 's' && release.getKeyCode() == KeyEvent.VK_S)
			{
				current_key = '\0';
				key_available = true;						
			}
			if(current_key == 'd' && release.getKeyCode() == KeyEvent.VK_D)
			{
				current_key = '\0';
				key_available = true;						
			}
		}
		
		else
		{
			if(user_inp == KeyEvent.VK_UP || user_inp == KeyEvent.VK_W)
			{
				UP = false;
				dy = 0;
				if(DOWN)
					dy = 1;
			}
			if(user_inp == KeyEvent.VK_LEFT || user_inp == KeyEvent.VK_A)
			{
				LEFT = false;
				dx = 0;
				if(RIGHT)
					dx = 1;
			}
			if(user_inp == KeyEvent.VK_DOWN || user_inp == KeyEvent.VK_S)
			{
				DOWN = false;
				dy = 0;
				if(UP)
					dy = -1;
			}
			if(user_inp == KeyEvent.VK_RIGHT || user_inp == KeyEvent.VK_D)
			{
				RIGHT = false;
				dx = 0;
				if(LEFT)
					dx = -1;
			}
			
			setDirection_arr(new int[]{dx, dy});
		}
	}

	public int[] getDirection_arr()
	{
		return direction_arr;
	}
	public void setDirection_arr(int[] direction_arr)
	{
		this.direction_arr = direction_arr;
	}
	
	// Slusk minigame methods
	public boolean isSpace_pressed()
	{
		return slusk_space_pressed;
	}
	public void setSpace_pressed(boolean space_pressed)
	{
		this.slusk_space_pressed = space_pressed;
	}

	public int getSlusk_points()
	{
		return slusk_points;
	}
	public void setSlusk_points(int slusk_points)
	{
		this.slusk_points = slusk_points;
	}
	
	public boolean isSlusk_active()
	{
		return slusk_active;
	}
	public void setSlusk_active(boolean slusk_active)
	{
		this.slusk_active = slusk_active;
	}
	
	// Attila minigame methods
	public void setAttila_active(boolean attila_active)
	{
		this.attila_active = attila_active;
	}
	
	public char getCurrent_key()
	{
		return current_key;
	}
	
	public boolean isKey_available()
	{
		return key_available;
	}
	public void setKey_available(boolean key_available)
	{
		this.key_available = key_available;
	}
}