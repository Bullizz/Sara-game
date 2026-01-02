package entities;

public class Enemy
{
	int x, y;
	int speed_x, speed_y;

	public int width, height;
	final public String id_string;
	public int follow_type = 3;
	public final int max_speed = 4;
	
	public Enemy(String id_string, int x, int y, int width, int height)
	{
		this.id_string 	 = id_string;
		this.x 			 = x;
		this.y			 = y;
		this.width		 = width;
		this.height		 = height;
	}
	
	public int getFollow_type()
	{
		return follow_type;
	}
	public void setFollow_type(int follow_type)
	{
		this.follow_type = follow_type;
	}

	public int getEnemy_x()
	{
		return x;
	}
	public void setEnemy_x(int enemy_x)
	{
		this.x = enemy_x;
	}
	
	public int getEnemy_y()
	{
		return y;
	}
	
	public void setEnemy_y(int enemy_y)
	{		
		this.y = enemy_y;
	}
	
	public int getSpeed_x()
	{
		return speed_x;
	}
	public void setSpeed_x(int speed_x)
	{
		this.speed_x = speed_x;
	}
	
	public int getSpeed_y()
	{
		return speed_y;
	}
	public void setSpeed_y(int speed_y)
	{
		this.speed_y = speed_y;
	}		
}