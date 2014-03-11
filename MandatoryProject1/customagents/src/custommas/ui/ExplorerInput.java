package custommas.ui;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

// Do not know Swing, code is more or less taken from Oracles tutorials

public class ExplorerInput {
	private String _agentName; 
	private JTextField textField;
	
	public ExplorerInput(String agentName, final IInputCallback callback){
		_agentName = agentName;

		//1. Create the frame.
		JFrame frame = new JFrame("Explorer: " + _agentName);

		//2. Optional: What happens when the frame closes?
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		
		JLabel label1 = new JLabel("Where to go?", JLabel.CENTER);
		
		//Set the position of the text, relative to the icon:
		label1.setVerticalTextPosition(JLabel.BOTTOM);
		label1.setHorizontalTextPosition(JLabel.CENTER);
		
	    textField = new JTextField(20);
	    textField.setHorizontalAlignment(JTextField.CENTER);
	    textField.addKeyListener(new KeyAdapter() {
	        public void keyPressed(KeyEvent e) {
		        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
		        	callback.inputReceived(textField.getText());
		        	textField.setText("");
		        }
	        }
	    });

		//3. Create components and put them in the frame.
		frame.getContentPane().add(label1, BorderLayout.NORTH);
		frame.getContentPane().add(textField, BorderLayout.CENTER);

		frame.pack();
		frame.setVisible(true);
	}
	
	public void showGoalFound(String goalNode){
		JFrame frame = new JFrame("Explorer: " + _agentName);

		//2. Optional: What happens when the frame closes?
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		JLabel label1 = new JLabel("GOAL NODE FOUND!", JLabel.CENTER);
		JLabel label2 = new JLabel(goalNode, JLabel.CENTER);
		
		//Set the position of the text, relative to the icon:
		label1.setVerticalTextPosition(JLabel.BOTTOM);
		label1.setHorizontalTextPosition(JLabel.CENTER);
		label2.setVerticalTextPosition(JLabel.BOTTOM);
		label2.setHorizontalTextPosition(JLabel.CENTER);

		//3. Create components and put them in the frame.
		frame.getContentPane().add(label1, BorderLayout.NORTH);
		frame.getContentPane().add(label2, BorderLayout.SOUTH);

		frame.pack();
		frame.setVisible(true);
	}
}
