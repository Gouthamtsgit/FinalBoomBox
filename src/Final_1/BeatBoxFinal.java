package Final_1;
import java.awt.*;
import javax.swing.*;
import java.io.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.net.*;
import javax.swing.event.*;


public class BeatBoxFinal 
{
   JFrame theFrame;
   JPanel mainPanel;
   JList incomingList;
   JTextField userMessage;
   ArrayList<JCheckBox> checkboxList;
   int nextNum;
   Vector<String> intVector=new Vector<String>();
   String userName;
   ObjectOutputStream out;
   ObjectInputStream in;
   HashMap<String, boolean[]> otherSeqMap=new HashMap<String,boolean[]>();
   Sequencer sequencer;
   Sequence sequence;
   Sequence mySequence=null;
   Track track;
   
   String[] instrumentName= {"Bass Drum","Closed Hi-hat","Open Hi-Hat","Acoustc Snare","Crash Cymbal",
		                     "Hand Clap","High Tom","Hi Bongo","Maracus","Whistle","Low Conga","CowBell","Vibraslap",
		                     "Low-mid Tom","High Agogo","Open Hi Conga"};
   
   int[] instruments= {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};
   public static void main(String[] args)
   {
	new BeatBoxFinal().startUp("% java BeatBoxFinal theFlash");
}
   public void startUp(String name)
   {
	   userName=name;
	   try
	   {
		   Socket sock=new Socket("127.0.0.1",4242);
		   out=new ObjectOutputStream(sock.getOutputStream());
		   in=new ObjectInputStream(sock.getInputStream());
		   Thread remote=new Thread(new RemoteReader());
		   remote.start();
	   }
	   catch(Exception e)
	   {
		   System.out.println("Could'nt connect - you'll have to play alone.");
	   }
	   setUpMidi();
	   buildGUI();
   }
   public void buildGUI()
   {
	   theFrame=new JFrame("Cyber BeatBox");
	   BorderLayout layout=new BorderLayout();
	   JPanel backGround=new JPanel(layout);
	   backGround.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
	   checkboxList =new ArrayList<JCheckBox>();
	   Box buttonBox=new Box(BoxLayout.Y_AXIS);
	   
	   JButton start=new JButton("START");
	   start.addActionListener(new MyStartListener());
	   buttonBox.add(start);
	   
	   JButton stop=new JButton("STOP");
	   stop.addActionListener(new MyStopListener());
	   buttonBox.add(stop);
	   
	   JButton TempoUp=new JButton("Temp Up");
	   TempoUp.addActionListener(new MyUpTempoListener());
	   buttonBox.add(TempoUp);
	   
	   JButton tempoDown=new JButton("Tempo Down");
	   tempoDown.addActionListener(new MyDownTempoListener());
	   buttonBox.add(tempoDown);
	   
	   JButton sendit=new JButton("SEND-IT");
	   sendit.addActionListener(new MysendListener());
	   buttonBox.add(sendit);
	   
	   userMessage=new JTextField();
	   buttonBox.add(userMessage);
	   
	   incomingList=new JList<>();
	   incomingList.addListSelectionListener(new MyListSelectionListener());
	   incomingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	   JScrollPane theList=new JScrollPane(incomingList);
	   buttonBox.add(theList);
	   incomingList.setListData(intVector);
	   
	   Box nameBox=new Box(BoxLayout.Y_AXIS);
	   for(int i=0;i<16;i++)
	   {
		   nameBox.add(new Label(instrumentName[i]));
	   }
	   backGround.add(BorderLayout.EAST,buttonBox);
	   backGround.add(BorderLayout.WEST,nameBox);
	   
	   theFrame.getContentPane().add(backGround);
	   GridLayout grid=new GridLayout(16,16);
	   grid.setVgap(1);
	   grid.setHgap(2);
	   mainPanel=new JPanel(grid);
	   backGround.add(BorderLayout.CENTER,mainPanel);
	   
	   for(int i=0;i<256;i++)
	   {
		   JCheckBox c=new JCheckBox();
		   c.setSelected(false);
		   checkboxList.add(c);
		   mainPanel.add(c);
	   }
	   theFrame.setBounds(50,50,50,50);
	   theFrame.pack();
	   theFrame.setVisible(true);
   }
   public void setUpMidi()
   {
	   try
	   {
		   sequencer=MidiSystem.getSequencer();
		   sequencer.open();
		   sequence=new Sequence(Sequence.PPQ, 4);
		   track=sequence.createTrack();
		   sequencer.setTempoInBPM(120);
	   }
	   catch(Exception e)
	   {
		   e.printStackTrace();
	   }
   }
   public void buildTrackAndStart()
   {
	   ArrayList<Integer> trackList=null;
	   sequence.deleteTrack(track);
	   track=sequence.createTrack();
	   for(int i=0;i<16;i++)
	   {
		   trackList=new ArrayList<Integer>();
		   for(int j=0;j<16;j++)
		   {
			   JCheckBox jc=(JCheckBox) checkboxList.get(j+(16*i));
			   if(jc.isSelected())
			   {
				   int key=instruments[i];
				   trackList.add(new Integer(key));
			   }
			   else
			   {
				   trackList.add(null);
			   }
		   }
		   makeTracks(trackList);
	   }
	   track.add(makeEvent(192,9,1,0,15));
	   try
	   {
		   sequencer.setSequence(sequence);
		   sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
		   sequencer.start();
		   sequencer.setTempoInBPM(120);
	   }
	   catch(Exception e)
	   {
		   e.printStackTrace();
	   }
   }
   public class MyStartListener implements ActionListener
   {
	   public void actionPerformed(ActionEvent a)
	   {
		   buildTrackAndStart();
	   }
   }
   public class MyStopListener implements ActionListener
   {
	   public void actionPerformed(ActionEvent b)
	   {
		   sequencer.stop();
	   }
   }
   public class MyUpTempoListener implements ActionListener
   {
	   public void actionPerformed(ActionEvent c)
	   {
		   float tempoFactor=sequencer.getTempoFactor();
		   sequencer.setTempoFactor((float)(tempoFactor*1.03));
	   }
   }
   public class  MyDownTempoListener implements ActionListener
   {
	   public void actionPerformed(ActionEvent d)
	   {
		   float tempoFactor=sequencer.getTempoFactor();
		   sequencer.setTempoFactor((float)(tempoFactor*0.97));
	   }
   }
   public class MysendListener implements ActionListener
   {
	   public void actionPerformed(ActionEvent e)
	   {
		   boolean[] checkboxState=new boolean[256];
		   for(int i=0;i<256;i++)
		   {
			   JCheckBox check=(JCheckBox) checkboxList.get(i);
			   if(check.isSelected())
			   {
				   checkboxState[i]=true;
			   }
		   }
		   String messageToSend=null;
		   try
		   {
			   out.writeObject(userName+nextNum++ + ":"+userMessage.getText());
			   out.writeObject(checkboxState);
		   }
		   catch(Exception e1)
		   {
			 System.out.println("Sorry bro. Could not send it to the server.");  
		   }
		   userMessage.setText("");
	   }
   }
   public class MyListSelectionListener implements ListSelectionListener
   {
	   public void valueChanged(ListSelectionEvent le)
	   {
		   if(!le.getValueIsAdjusting())
		   {
			   String selected=(String) incomingList.getSelectedValue();
			   if(selected!=null)
			   {
				   boolean[] selectedState=(boolean[]) otherSeqMap.get(selected);
				   changeSequence(selectedState);
				   sequencer.stop();
				   buildTrackAndStart();
			   }
		   }
	   }
   }
   public class RemoteReader implements Runnable
   {
	   boolean[] checkboxState=null;
	   String nameToShow=null;
	   Object obj=null;
	   public void run()
	   {
		   try
		   {
			   while((obj=in.readObject())!=null)
			   {
				   System.out.println("Got an Object from Server");
				   System.out.println(obj.getClass());
				   String nameToShow=(String) obj;
				   checkboxState=(boolean[]) in.readObject();
				   otherSeqMap.put(nameToShow,checkboxState);
				   intVector.add(nameToShow);
				   incomingList.setListData(intVector);
			   }
		   }
		   catch(Exception e2)
		   {
			   e2.printStackTrace();
		   }
	   }
   }
   public class MyPlayineListener implements ActionListener
   {
	   public void actionPerformed(ActionEvent g)
	   {
		   if(mySequence!=null)
		   {
			   sequence=mySequence;
		   }
	   }
   }
   public void changeSequence(boolean[] checkboxState)
   {
	   for(int i=0;i<256;i++)
	   {
		   JCheckBox check=(JCheckBox) checkboxList.get(i);
		   if(checkboxState[i])
		   {
			   check.setSelected(true);
		   }
		   else
		   {
			   check.setSelected(false);
		   }
	   }
   }
   public void makeTracks(ArrayList list)
   {
	  java.util.Iterator it=list.iterator();
	   for(int i=0;i<16;i++)
	   {
		   Integer num=(Integer) it.next();
		   if(num!=null)
		   {
			   int numKey=num.intValue();
			   track.add(makeEvent(144,9,numKey,100,1));
			   track.add(makeEvent(128,9,numKey,100,i+1));
		   }
	   }
   }
   public MidiEvent makeEvent(int comd,int chan,int one,int two,int tick)
   {
	   MidiEvent event=null;
	   try
	   {
		   ShortMessage a=new ShortMessage();
		   a.setMessage(comd,chan,one,two);
		   event=new MidiEvent(a, tick);
	   }
	   catch(Exception e)
	   {
		   
	   }
	   return event;
   }
}
