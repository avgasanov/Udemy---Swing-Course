package gui;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;

public class Toolbar extends JToolBar  implements ActionListener {
	
	private JButton saveButton;
	private JButton refreshButton;
	private ToolbarListener textListener;
	
	public Toolbar() {
		setBorder(BorderFactory.createEtchedBorder()); // no dragable/dragable
		setFloatable(false); // make floatable 
		
		saveButton = new JButton();
		saveButton.setIcon(Utils.createIcon("/images/Save16.gif"));
		saveButton.setToolTipText("Save");
		
		refreshButton = new JButton();
		refreshButton.setIcon(Utils.createIcon("/images/Refresh16.gif"));
		refreshButton.setToolTipText("Refresh");
		
		saveButton.addActionListener(this);
		refreshButton.addActionListener(this);
		
		//setLayout(new FlowLayout(FlowLayout.LEFT));
		
		add(saveButton);
		//addSeparator();
		add(refreshButton);
		
		
	}
	
	
	public void setToolbarListener(ToolbarListener listener) {
		this.textListener = listener;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton clicked = (JButton) e.getSource();
		
		if(clicked == saveButton) {
			if (textListener != null) {
				textListener.saveEventOcurred();
			}
		} else if (clicked == refreshButton) {
			if (textListener != null) {
				textListener.refreshEventOcurred();
			}
		}
	}

}
