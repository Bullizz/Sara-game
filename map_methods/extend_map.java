import java.awt.Color;
import java.awt.GridLayout;
import java.io.File;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class extend_map
{	
	public static void main(String[] args)
	{
		int[][] int_map = loadMapConstraints();
		
		String[] compressed_map = compressMap(int_map);
		dispMap(compressed_map);
		
//		int[][] extended_int_map = extendMap(int_map);
//		String[] extended_compressed_map = compressed_map(extended_int_map);
	}
	
	private static String[] compressMap(int[][] int_map)
	{
		int rows = int_map.length;
		int cols = int_map[0].length;
		
		String[] compressed_map = new String[rows];
		int i, j;
		StringBuffer sb;
		for(i = 0; i < rows; i++)
		{
			sb = new StringBuffer();
			sb.append("#");
			int counter = 0;
			
			for(j = 1; j < cols; j++)
			{
				if(int_map[i][j] != int_map[i][j - 1])
				{
					sb.append(counter);
					if(int_map[i][j - 1] == 1)
						sb.append("W");
					else
						sb.append("B");
					
					sb.append("#");
					counter = 0;
				}
				
				counter++;
			}
			sb.append(counter);
			if(int_map[i][j - 1] == 1)
				sb.append("W");
			else
				sb.append("B");
			
			compressed_map[i] = sb.toString();
//			System.out.println(sb.toString());
		}
	
		return compressed_map;
	}

	private static int[][] loadMapConstraints()
	{
		int[][] map = new int[972][1921];
		int i = 0;
		int j = 0;
		
		try
		{
			File file = new File("map-1.txt");
			Scanner reader = new Scanner(file);
			while(reader.hasNextLine())
			{
				String curr_line = reader.nextLine();
				char[] ch_curr_line = curr_line.toCharArray();
				for(int k = 0; k < ch_curr_line.length; k++)
				{
					if(ch_curr_line[k] == '0')// && j < this.width)
					{
						map[i][j] = 0;
						j++;
					}
					else if(ch_curr_line[k] == '1')// && j < this.width)
					{
						map[i][j] = 1;
						j++;
					}
//					System.out.println(ch_curr_line[k]);
				}
				i++;
				j = 0;
			}
			reader.close();
		} catch(Exception file_except)
		{
			file_except.printStackTrace();
		}
		
		return map;
	}

	private static void dispMap(String[] compressed_map)
	{
		int[][] plotting_data = new int[compressed_map.length][4];


		for(int rows = 0; rows < compressed_map.length; rows++)
		{
			int x = 0;
			char[] curr_row = compressed_map[rows].toCharArray();
			for(int j = 0; j < curr_row.length; j++)
			{
				JLabel p = new JLabel(" ", JLabel.CENTER);
				int color = 0;
				String amount_str = "";
				
				char curr_char = curr_row[j];
//				System.out.println();
				if(curr_char == 'W')
				{
					int k = j;
					while(curr_row[k] != '#')
						k--;
					amount_str = compressed_map[rows].substring(k + 1, j);
					color = 250;
//					p.setText("W");
				}
				else if(curr_char == 'B')
				{
					int k = j;
					while(curr_row[k] != '#')
						k--;
					amount_str = compressed_map[rows].substring(k + 1, j);
					color = 0;
//					p.setText("B");
				}

				int amount = 0;
				try
				{
					amount = Integer.valueOf(amount_str);	
				} catch(Exception e)
				{
//					System.err.println("bruh");
//					e.printStackTrace();
				}
				if(amount != 0)
				{
					plotting_data[rows][0] = amount;
					plotting_data[rows][1] = x;
					plotting_data[rows][2] = rows;
					plotting_data[rows][3] = color;
					
					x += amount;
				}
			}
		}

		JFrame frame = new JFrame();
		frame.setUndecorated(true);
		frame.setSize(1920, 972);
//		frame.setLocationRelativeTo(null);
		frame.setLocation(0, 1 * (972 / 10));
		frame.setLayout(new GridLayout(1, 1));
		frame.setBackground(new Color(255, 255, 255, 1));
		frame.add(new map_frame(plotting_data));
		frame.setVisible(true);

		System.out.println("Framed");
	}
}