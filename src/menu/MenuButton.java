package menu;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.border.Border;

public class MenuButton extends JButton implements /*ActionListener, */MouseListener
{
	Color blue = new Color(0, 162, 232);
	Color yellow = new Color(239, 228, 176);
	
	String btn_name;
	
	Border border = BorderFactory.createMatteBorder(4, 4, 4, 4, yellow);
	Border hover_border = BorderFactory.createMatteBorder(7, 7, 7, 7, yellow);
	
	Font font = new Font("Arial", Font.PLAIN, 40);
	Font hover_font = new Font("Arial", Font.BOLD, 40);
	
	public MenuButton(String btn_name)
	{
		super(btn_name);
		
		this.btn_name = btn_name;
		
		new JButton();
		setBackground(blue);
		setForeground(yellow);
		setBorder(border);
		setFont(font);
		addMouseListener(this);
	}
	
//	@Override
//	public void actionPerformed(ActionEvent e){}
	
	@Override
	public void mouseClicked(MouseEvent e){}
	@Override
	public void mousePressed(MouseEvent e)
	{
		switch(btn_name)
		{
		case "Main Menu":
			JOptionPane.showMessageDialog(null, "Main Menu");
			break;
		case "Exit":
			System.exit(0);
			break;
		}
	}
	@Override
	public void mouseReleased(MouseEvent e){}
	@Override
	public void mouseEntered(MouseEvent e)
	{
		setFont(hover_font);
		setBorder(hover_border);
	}
	@Override
	public void mouseExited(MouseEvent e)
	{		
		setFont(font);
		setBorder(border);
	}
}