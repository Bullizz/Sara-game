package entities;

public class PLAYER
{
	int player_x, player_y;
	
	final public int player_width, player_height;
	final public int max_speed = 10;
	
	int player_speed_x = 0;
	int player_speed_y = 0;
	
	public int player_dx = 1;
	public int player_dy = 1;

	public PLAYER(int x, int y, int width, int height)
	{
		player_x = x;
		player_y = y;
		player_width = width;
		player_height = height;
	}
	
	public int getPlayer_x()
	{
		return player_x;
	}
	public void setPlayer_x(int player_x)
	{
		this.player_x = player_x;
	}

	public int getPlayer_y()
	{
		return player_y;
	}
	public void setPlayer_y(int player_y)
	{
		this.player_y = player_y;
	}

	public int getPlayer_speed_x()
	{
		return player_speed_x;
	}
	public void setPlayer_speed_x(int player_speed_x)
	{
		this.player_speed_x = player_speed_x;
	}
	
	public int getPlayer_speed_y()
	{
		return player_speed_y;
	}
	public void setPlayer_speed_y(int player_speed_y)
	{
		this.player_speed_y = player_speed_y;
	}
}