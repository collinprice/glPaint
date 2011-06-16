package glPaint;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.nio.IntBuffer;
import java.util.LinkedList;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLJPanel;
import javax.media.opengl.glu.GLU;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalToggleButtonUI;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.BufferUtil;

import net.miginfocom.swing.MigLayout;

/**
 * 
 * @author Collin Price
 * @course DA2042
 * @date 
 * @assign Practical Assignment
 *
 * IMPORTANT NOTES ABOUT CODE
 * 
 * - not all variable names make sense since I 
 *   did some last minute changes.
 * - there are some unimplemented features:
 *    - CustomColor would not work properly with OpenGL
 *    - Rotate is not available
 *
 */

@SuppressWarnings("serial")
public class GLPaint extends JFrame implements ComponentListener {

	private JPanel mainPanel;
	private JPanel buttonPanel;
	private JPanel drawArea;
	private JPanel colourArea;
	private JPanel currentColour;
	private JPanel optionPanel;
	private JLabel coords;
	private GLJPanel glPanel;
	
	private JColorChooser tcc;
	private GLPaint painter = this;
	private GLU glu = new GLU();
	private Animator anim;
	
	private Renderer rend;
	private ModeSelectListener modeSelector = new ModeSelectListener();
	private ButtonListener buttonHit = new ButtonListener();
	
	private boolean BUTTON_DOWN;
	private JButton currentMode;
	
    static final int MIN_WIDTH = 500;
    static final int MIN_HEIGHT = 450;
	
    public static void main (String[] args) {
		new GLPaint();
	}
    
	public GLPaint() {
		// ++Configure Window++
		setTitle ("GLPaint"); 
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocation(150, 150);
		addComponentListener(this);
		// --Configure Window--
		
		// ++User Interface++
		initMenuBar();
		mainPanel = new JPanel(new MigLayout()); // main panel
		buttonPanel = new JPanel(new MigLayout());
		drawArea = new JPanel(new MigLayout());
		colourArea = new JPanel(new MigLayout());
		add(mainPanel);
		initButtons();
		initDraw();
		initColours();
		
		mainPanel.add(buttonPanel, "top");
		mainPanel.add(drawArea, "wrap");
		mainPanel.add(colourArea, "alignx 75, span 2 1");
		anim.start();
		// --User Interface--
		// ++Configure Window++
		pack();
		setVisible(true); // Display window
		
		// --Configure Window--
	} // constructor
	
	

	private void initDraw() {
		
		GLCapabilities caps = new GLCapabilities();
		caps.setDoubleBuffered(true);
	    caps.setHardwareAccelerated(true);
		glPanel = new GLJPanel(caps);
		glPanel.setPreferredSize(new Dimension(800,600));
		glPanel.setIgnoreRepaint(true);
		rend = new Renderer();

		glPanel.addGLEventListener(rend);
		glPanel.addMouseMotionListener(rend);
		glPanel.addMouseListener(rend);
		drawArea.add(glPanel);
		anim = new Animator(glPanel);
		
	}
	
	private void initColours() {
		ButtonGroup selectableColours = new ButtonGroup();
		
		JToggleButton whiteColour = createColour(Color.WHITE);
		selectableColours.add(whiteColour);
		JToggleButton blackColour = createColour(Color.BLACK);
		selectableColours.add(blackColour);
		JToggleButton redColour = createColour(Color.red);
		selectableColours.add(redColour);
		JToggleButton blueColour = createColour(Color.blue);
		selectableColours.add(blueColour);
		JToggleButton greenColour = createColour(Color.green);
		selectableColours.add(greenColour);
		JToggleButton yellowColour = createColour(Color.yellow);
		selectableColours.add(yellowColour);
		JToggleButton cyanColour = createColour(Color.cyan);
		selectableColours.add(cyanColour);
		JToggleButton magentaColour = createColour(Color.magenta);
		selectableColours.add(magentaColour);
		JToggleButton orangeColour = createColour(Color.orange);
		selectableColours.add(orangeColour);
		//JToggleButton pinkColour = createColour(Color.lightGray);
		//selectableColours.add(pinkColour);
		
		JButton custom = new JButton("Custom Colour");
		custom.setActionCommand("cColour");
		custom.addActionListener(new ButtonListener());
		
		coords = new JLabel();
		
		//Set up color chooser for setting text color
        tcc = new JColorChooser();
        tcc.getSelectionModel().addChangeListener(new CustomColourListener());
        tcc.setBorder(BorderFactory.createTitledBorder("Choose Text Color"));
		
		colourArea.add(whiteColour, "width 30px!, height 30px!");
		colourArea.add(blackColour, "width 30px!, height 30px!");
		colourArea.add(redColour, "width 30px!, height 30px!");
		colourArea.add(blueColour, "width 30px!, height 30px!");
		colourArea.add(greenColour, "width 30px!, height 30px!");
		colourArea.add(yellowColour, "width 30px!, height 30px!");
		colourArea.add(cyanColour, "width 30px!, height 30px!");
		colourArea.add(magentaColour, "width 30px!, height 30px!");
		colourArea.add(orangeColour, "width 30px!, height 30px!");
		//colourArea.add(pinkColour, "width 30px!, height 30px!");
		
		colourArea.add(custom);
		colourArea.add(coords);
		
	}
	
	private JToggleButton createColour(Color c) {
		Border border = BorderFactory.createEmptyBorder(4,4,4,4);
		
		JToggleButton colourS = new JToggleButton();
		JPanel inside = new JPanel();
		inside.setBackground(c);
		inside.setPreferredSize(new Dimension(20,20));
		colourS.setUI(new customUI());
		colourS.setActionCommand("" + c.getRGB());
		colourS.addActionListener(new ColourListener());
		colourS.setBorder(border);
		colourS.setBackground(Color.WHITE);
		colourS.add(inside);
		
		
		return colourS;
	}

	private void initButtons() {
		JButton pointerB = new JButton("Select");
		JButton eraseB = new JButton("Erase");
		JButton fillB = new JButton("Zoom");
		JButton dropperB = new JButton("Scale");
		JButton lineB = new JButton(new ImageIcon("images/line.jpg"));
		JButton circleB = new JButton(new ImageIcon("images/circle.jpg"));
		JButton squareB = new JButton(new ImageIcon("images/square.jpg"));
		JButton polygonB = new JButton(new ImageIcon("images/polygon.jpg"));
		JButton zoominB = new JButton("Translate");
		JButton zoomoutB = new JButton("Rotate");
		
		pointerB.setActionCommand("pointer");
		pointerB.addActionListener(modeSelector);
		pointerB.setBorder(BorderFactory.createRaisedBevelBorder());
		pointerB.setBackground(Color.LIGHT_GRAY);
		eraseB.setActionCommand("erase");
		eraseB.addActionListener(modeSelector);
		eraseB.setBorder(BorderFactory.createRaisedBevelBorder());
		eraseB.setBackground(Color.LIGHT_GRAY);
		fillB.setActionCommand("fill");
		fillB.addActionListener(modeSelector);
		fillB.setBorder(BorderFactory.createRaisedBevelBorder());
		fillB.setBackground(Color.LIGHT_GRAY);
		dropperB.setActionCommand("dropper");
		dropperB.addActionListener(modeSelector);
		dropperB.setBorder(BorderFactory.createRaisedBevelBorder());
		dropperB.setBackground(Color.LIGHT_GRAY);
		lineB.setActionCommand("line");
		lineB.addActionListener(modeSelector);
		lineB.setBorder(BorderFactory.createRaisedBevelBorder());
		lineB.setBackground(Color.LIGHT_GRAY);
		circleB.setActionCommand("circle");
		circleB.addActionListener(modeSelector);
		circleB.setBorder(BorderFactory.createRaisedBevelBorder());
		circleB.setBackground(Color.LIGHT_GRAY);
		squareB.setActionCommand("square");
		squareB.addActionListener(modeSelector);
		squareB.setBorder(BorderFactory.createRaisedBevelBorder());
		squareB.setBackground(Color.LIGHT_GRAY);
		polygonB.setActionCommand("polygon");
		polygonB.addActionListener(modeSelector);
		polygonB.setBorder(BorderFactory.createRaisedBevelBorder());
		polygonB.setBackground(Color.LIGHT_GRAY);
		zoominB.setActionCommand("zoomin");
		zoominB.addActionListener(modeSelector);
		zoominB.setBorder(BorderFactory.createRaisedBevelBorder());
		zoominB.setBackground(Color.LIGHT_GRAY);
		zoomoutB.setActionCommand("zoomout");
		zoomoutB.addActionListener(modeSelector);
		zoomoutB.setBorder(BorderFactory.createRaisedBevelBorder());
		zoomoutB.setBackground(Color.LIGHT_GRAY);
		
		currentMode = lineB;
		lineB.setBorder(BorderFactory.createLoweredBevelBorder());
		
		buttonPanel.add(pointerB, "width 60px!, height 30px!");
		buttonPanel.add(eraseB, "width 60px!, height 30px!, wrap");
		buttonPanel.add(dropperB, "width 60px!, height 30px!");
		buttonPanel.add(fillB, "width 60px!, height 30px!, wrap");
		buttonPanel.add(zoominB, "width 60px!, height 30px!");
		buttonPanel.add(zoomoutB, "width 60px!, height 30px!, wrap");
		buttonPanel.add(lineB, "width 60px!, height 30px!");
		buttonPanel.add(circleB, "width 60px!, height 30px!, wrap");
		buttonPanel.add(squareB, "width 60px!, height 30px!");
		buttonPanel.add(polygonB, "width 60px!, height 30px!, wrap");
		
		optionPanel = new JPanel(new MigLayout());
		optionPanel.setBackground(Color.LIGHT_GRAY);
		buttonPanel.add(optionPanel, "width 65, height 65, span 2, wrap");
		
		currentColour = new JPanel(new MigLayout());
		Border border = BorderFactory.createLineBorder(Color.BLACK);
		currentColour.setBorder(border);
		currentColour.setBackground(Color.BLACK);
		buttonPanel.add(currentColour, "width 65, height 65, span 2");
		
		
	}

	private void initMenuBar() {
		JMenuBar menuBar;
		JMenu menu;
		JMenuItem menuItem;
		
		menuBar = new JMenuBar(); // menu bar
		menu = new JMenu("File"); // menu option
		menuBar.add(menu);
		menuItem = new JMenuItem("Save"); // item in drop down
		menuItem.setMnemonic('s'); // short cut
		menuItem.addActionListener(buttonHit);
		menu.add(menuItem);
		menuItem = new JMenuItem("Exit"); // item in drop down
		menuItem.setMnemonic('x'); // short cut
		menuItem.addActionListener(buttonHit);
		menu.add(menuItem);
		
		menu = new JMenu("Edit"); // menu option
		menuBar.add(menu);
		menuItem = new JMenuItem("Custom Colour"); // item in drop down
		menuItem.setMnemonic('c'); // short cut
		menuItem.setActionCommand("cColour");
		menuItem.addActionListener(buttonHit);
		menu.add(menuItem);
		
		setJMenuBar(menuBar);
	}

	
	/**
	 * Renderer handles EVERYTHING
	 * 
	 * It draws in the glPanel.
	 * 
	 * Detected all mouse events; MouseListener and MouseMotionListener.
	 * 
	 * @author collin
	 *
	 */
	private class Renderer implements GLEventListener, MouseListener, MouseMotionListener {

		private GL gl;
		private MouseEvent me;
		private LinkedList<Coords> list = new LinkedList<Coords>();
		private LinkedList<Shape> shapes = new LinkedList<Shape>();
		private int names = 1;
		private Point pickPoint = new Point();
		private int selectedObject = -1;
		private int tfmType = -1;
		
		private int originalWidth;
		private int originalHeight;
		
		private boolean ZOOMED = false;
		
		private Point start;
		
		/**
		 * TFM is a simple class that stores
		 * different transformation types.
		 * 
		 * @author collin
		 *
		 */
		private class TFM {
			public static final int TRANSLATE = 1;
			public static final int SCALE = 2;
			public static final int ROTATE = 3;
		}
		
		/**
		 * Coords stores a vertex along with its Color
		 * at the time of creation.
		 * @author collin
		 *
		 */
		private class Coords {
			
			float x;
			float y;
			Color color;
			
			public Coords(float x, float y, Color c) {
				this.x = x;
				this.y = y;
				this.color = c;
			}
			
		} // cords
		
		/**
		 * Shape is an abstract class that defines the basic primitives for all shapes.
		 * @author collin
		 *
		 */
		public abstract class Shape {
			
			protected Color colour;
			protected int name;
			protected int type;
			protected LinkedList<Coords> coords = new LinkedList<Coords>();
			
			//translate
			float tx = 0.0f;
			float ty = 0.0f;
			float ltx;
			float lty;
			
			//scale
			float sx = 1.0f;
			float sy = 1.0f;
			
			float anglef = 0.0f;
			
			//center of object
			float midx = -1;
			float midy = -1;
			
			public Shape (int t, Color c, int n) {
				this.colour = c;
				this.name = n;
				this.type = t;
			} // constructor
			
			public abstract void addCoord(Coords coord);
			
			public abstract void draw(GL gl, int mode);
			
		} //shape
		
		/**
		 * Polygon implements Lines, Squares and Polygons.
		 * 
		 * A polygon is created and then vertices are added.
		 * 
		 * A single draw method was created to draw every type
		 * of polygon.
		 * @author collin
		 *
		 */
		public class Polygon extends Shape {

			
			public Polygon(int type, Color c, int n) {
				super(type, c, n);
			}

			@Override
			public void addCoord(Coords coord) {
				coords.add(coord);
			}

			@Override
			public void draw(GL gl, int mode) {
				if (midx == -1) {
					
					for (Coords c : coords) {
						midx += c.x;
						midy += c.y;
					}
					midx /= coords.size();
					midy /= coords.size();
				}
				
				if (selectedObject == this.name) {
					
					if (tfmType == TFM.ROTATE) {
						
					} else if (tfmType == TFM.SCALE) {
						if (me.getX() - start.x > 0 && start.x != me.getX()) {
							sx = sx + 0.05f;
							sy = sy + 0.05f;
							start.x = me.getX();
						} else if (me.getX() - start.x < 0 && start.x != me.getX()){
							sx = sx - 0.05f;
							sy = sy - 0.05f;
							start.x = me.getX();
						}
						
					} else if (tfmType == TFM.TRANSLATE) {
						tx = (me.getX() - start.x) + ltx;
						ty = (me.getY() - start.y) + lty;
					}
					
				} else {
					if (ltx != tx) {
						ltx = tx;
						lty = ty;
					}
				}
				
				gl.glPushMatrix();
				if (mode == GL.GL_SELECT) {
					gl.glLoadName(this.name);
				}
				
				
				gl.glTranslatef(tx, ty, 0.0f);
				
				gl.glTranslatef(midx, midy, 0.0f);
				gl.glScalef(sx, sy, 0.0f);
				gl.glRotatef(anglef, 0.0f, 0.0f, 1.0f);
				gl.glTranslatef(-midx, -midy, 0.0f);
				
				
				
				gl.glBegin(type);
				gl.glColor3f(colour.getRed(), colour.getGreen(), colour.getBlue());
				for (Coords coord : coords) {
					gl.glColor3f(coord.color.getRed(), coord.color.getGreen(), coord.color.getBlue());
					gl.glVertex3f(coord.x, coord.y, 0f);
				}
				gl.glEnd();
				
				if (selectedObject == this.name) {
					if (type == GL.GL_LINES) {
						gl.glEnable(GL.GL_LINE_WIDTH);
						gl.glLineWidth(4.0f);
						gl.glBegin(GL.GL_LINES);
						for (Coords coord : coords) {
							gl.glVertex3f(coord.x, coord.y, 0f);
						}
						gl.glEnd();
						gl.glLineWidth(1.0f);
						gl.glDisable(GL.GL_LINE_WIDTH);
					} else {
						gl.glEnable(GL.GL_LINE_STIPPLE);
						gl.glLineStipple(1, (short) 255);
						gl.glBegin(GL.GL_LINE_LOOP);
						gl.glColor3f(0.0f, 0.0f, 0.0f);
						for (Coords coord : coords) {
							gl.glVertex3f(coord.x, coord.y, 0f);
						}
						gl.glEnd();
						gl.glDisable(GL.GL_LINE_STIPPLE);
					}
				}
				gl.glPopMatrix();
			} //draw
			
		} // polygon
		
		public class Circle extends Shape {

			float radius;
			
			public Circle(Color c, int n) {
				super(GL.GL_POLYGON, c, n);
			}

			@Override
			public void addCoord(Coords coord) {
				coords.add(coord);
			}
			
			public void addRadius(float r) {
				this.radius = r;
			}

			@Override
			public void draw(GL gl, int mode) {
				if (midx == -1) {
					midx = coords.getFirst().x;
					midy = coords.getFirst().y;
				}
				if (selectedObject == this.name) {
					
					if (tfmType == TFM.ROTATE) {
						
					} else if (tfmType == TFM.SCALE) {
						if (me.getX() - start.x > 0 && start.x != me.getX()) {
							sx = sx + 0.05f;
							sy = sy + 0.05f;
							start.x = me.getX();
						} else if (me.getX() - start.x < 0 && start.x != me.getX()){
							sx = sx - 0.05f;
							sy = sy - 0.05f;
							start.x = me.getX();
						}
					} else if (tfmType == TFM.TRANSLATE) {
						tx = (me.getX() - start.x) + ltx;
						ty = (me.getY() - start.y) + lty;
					}
					
					
				} else {
					if (ltx != tx) {
						ltx = tx;
						lty = ty;
					}
				}
				
				gl.glPushMatrix();
				if (mode == GL.GL_SELECT) {
					gl.glLoadName(this.name);
				}
				
				gl.glTranslatef(tx, ty, 0.0f);
				
				
				gl.glTranslatef(midx, midy, 0.0f);
				gl.glRotatef(anglef, 0.0f, 0.0f, 1.0f);
				gl.glScalef(sx, sy, 0.0f);
				gl.glTranslatef(-midx, -midy, 0.0f);
				
				gl.glBegin(type);
				Coords c = coords.getFirst();
				gl.glColor3f(colour.getRed(), colour.getGreen(), colour.getBlue());
				for( float angle = 0; angle < 2*Math.PI; angle += 0.01 ) {
					gl.glVertex3f( (float)(c.x + radius*Math.cos(angle)), (float)(c.y + radius*Math.sin(angle)), 0.0f );
				}
				gl.glEnd();
				
				if (selectedObject == this.name) {
					gl.glEnable(GL.GL_LINE_STIPPLE);
					gl.glLineStipple(1, (short) 255);
					gl.glBegin(GL.GL_LINE_LOOP);
					gl.glColor3f(0.0f, 0.0f, 0.0f);
					for( float angle = 0; angle < 2*Math.PI; angle += 0.01 ) {
						gl.glVertex3f( (float)(c.x + radius*Math.cos(angle)), (float)(c.y + radius*Math.sin(angle)), 0.0f );
					}
					gl.glEnd();
					gl.glDisable(GL.GL_LINE_STIPPLE);
				}
				
				gl.glPopMatrix();
			}
			
		}
		
		public void draw(GL gl) {
			/* 
			 * POINTER COMMANDS AFTER BUTTON HAS BEEN PRESSED
			 */
			if (currentMode.getActionCommand().equals("pointer")) {
				int objectHit = picker(gl);
				if (objectHit > 0) {
					selectedObject = objectHit;
					for (int i = 0; i < shapes.size(); i++) {
						if (shapes.get(i).name == objectHit) {
							Shape temp = shapes.remove(i);
							shapes.add(temp);
						}
					}
				} else {
					selectedObject = -1;
				}
			} else if (currentMode.getActionCommand().equals("line")) {
				if (list.size() == 0) {
					list.add(new Coords(me.getX(), me.getY(), currentColour.getBackground()));
					gl.glBegin(GL.GL_POINT);
					gl.glColor3f(currentColour.getBackground().getRed(), currentColour.getBackground().getGreen(), currentColour.getBackground().getBlue());
					gl.glVertex3f(me.getX(), me.getY(), 0f);
					gl.glEnd();
				} else if (list.size() == 1) {
					list.add(new Coords(me.getX(), me.getY(), currentColour.getBackground()));
					Polygon p = new Polygon(GL.GL_LINES, currentColour.getBackground(), names++);
					for (Coords c : list) {
						p.addCoord(c);
					}
					shapes.add(p);
					list.clear();
				}
			} else if (currentMode.getActionCommand().equals("erase")) {
				int objectHit = picker(gl);
				if (objectHit > 0) {
					for (int i = 0; i < shapes.size(); i++) {
						if (shapes.get(i).name == objectHit) {
							shapes.remove(i);
							break;
						}
					}
				}
			} else if (currentMode.getActionCommand().equals("fill")) {
				if ((me.getModifiers() & MouseEvent.BUTTON1_MASK) == MouseEvent.BUTTON1_MASK) {
					if (!ZOOMED) {
						double ratio;
						if (glPanel.getWidth() > glPanel.getHeight()) {
							ratio = glPanel.getWidth()/glPanel.getHeight();
						} else {
							ratio = glPanel.getHeight()/glPanel.getWidth();
						}
						
					    gl.glMatrixMode(GL.GL_PROJECTION);
					    gl.glLoadIdentity();
					    gl.glOrtho((me.getX()-(0.25*ratio)*glPanel.getWidth()), me.getX()+(0.25*ratio)*glPanel.getWidth(), me.getY()+(0.25*glPanel.getHeight()), me.getY()-(0.25*glPanel.getHeight()), -0.5, 2.5);
					    gl.glMatrixMode(GL.GL_MODELVIEW);
					    gl.glLoadIdentity();
					    ZOOMED = true;
					}
					
					
				} else if ((me.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {
					if (ZOOMED) {
						gl.glViewport(0, 0, originalWidth, originalHeight);
					    gl.glMatrixMode(GL.GL_PROJECTION);
					    gl.glLoadIdentity();
					    gl.glOrtho(0.0, originalWidth, originalHeight, 0.0, -0.5, 2.5);
					    gl.glMatrixMode(GL.GL_MODELVIEW);
					    gl.glLoadIdentity();
					    ZOOMED = false;
					}
				}
				
			} else if (currentMode.getActionCommand().equals("dropper")) {
				if (start == null ) {
					int objectHit = picker(gl);
					if (objectHit > 0) {
						selectedObject = objectHit;
						tfmType = TFM.SCALE;
						start = me.getPoint();
					}
				} else {
					selectedObject = -1;
					tfmType = -1;
					start = null;
				}
			} else if (currentMode.getActionCommand().equals("circle")) {
				if (list.size() == 0) {
					list.add(new Coords(me.getX(), me.getY(), currentColour.getBackground()));
					gl.glBegin(GL.GL_POINT);
					gl.glColor3f(currentColour.getBackground().getRed(), currentColour.getBackground().getGreen(), currentColour.getBackground().getBlue());
					gl.glVertex3f(me.getX(), me.getY(), 0f);
					gl.glEnd();
				} else if (list.size() == 1) {
					Circle c = new Circle(currentColour.getBackground(), names++);
					c.addCoord(list.getFirst());
					c.addRadius((float) Point2D.distance(list.getFirst().x, list.getFirst().y, me.getX(), me.getY()));
					list.clear();
					shapes.add(c);
				}
			} else if (currentMode.getActionCommand().equals("square")) {
				if (list.size() == 0) {
					list.add(new Coords(me.getX(), me.getY(), currentColour.getBackground()));
					gl.glBegin(GL.GL_POINT);
					gl.glColor3f(currentColour.getBackground().getRed(), currentColour.getBackground().getGreen(), currentColour.getBackground().getBlue());
					gl.glVertex3f(me.getX(), me.getY(), 0f);
					gl.glEnd();
				} else if ( list.size() == 1 ) {
					// SECOND VERTEX OF SQUARE OBJECT
					Polygon p = new Polygon(GL.GL_POLYGON, currentColour.getBackground(), names++);
					list.add(new Coords(me.getX(), list.get(0).y, currentColour.getBackground()));
					list.add(new Coords(me.getX(), me.getY(), currentColour.getBackground()));
					list.add(new Coords(list.get(0).x, me.getY(), currentColour.getBackground()));
					for (Coords c : list) {
						p.addCoord(c);
					}
					list.clear();
					shapes.add(p);
				}
			} else if (currentMode.getActionCommand().equals("polygon")) {
				if (list.size() == 0) {
					list.add(new Coords(me.getX(), me.getY(), currentColour.getBackground()));
					gl.glBegin(GL.GL_POINT);
					gl.glColor3f(currentColour.getBackground().getRed(), currentColour.getBackground().getGreen(), currentColour.getBackground().getBlue());
					gl.glVertex3f(me.getX(), me.getY(), 0f);
					gl.glEnd();
				} else if (list.size() > 0) {
					list.add(new Coords(me.getX(), me.getY(), currentColour.getBackground()));
					
					if (near(list.getFirst(), list.getLast()) || (me.getModifiers() & MouseEvent.BUTTON3_MASK) == MouseEvent.BUTTON3_MASK) {
						list.removeLast();
						Polygon p = new Polygon(GL.GL_POLYGON, currentColour.getBackground(), names++);
						for (Coords c : list) {
							p.addCoord(c);
						}
						shapes.add(p);
						list.clear();
					}
				}
			} else if (currentMode.getActionCommand().equals("zoomin")) {
				if (start == null ) {
					int objectHit = picker(gl);
					if (objectHit > 0) {
						selectedObject = objectHit;
						tfmType = TFM.TRANSLATE;
						start = me.getPoint();
					}
				} else {
					selectedObject = -1;
					tfmType = -1;
					start = null;
				}
				
			} else if (currentMode.getActionCommand().equals("zoomout")) {
				if (tfmType != TFM.ROTATE) {
					int objectHit = picker(gl);
					if (objectHit > 0) {
						selectedObject = objectHit;
						tfmType = TFM.ROTATE;
					}
				} else {
					selectedObject = -1;
					tfmType = -1;
				}
				
			}
			
		}
		
		private boolean near(Coords first, Coords last) {
			return (Math.abs(first.x - last.x) < 5) && (Math.abs(first.y - last.y) < 5);
		}
		
		public void drawAll(GL gl, int mode) {
			/*
			 * REDRAWING EACH OBJECT
			 */
			for (Shape s : shapes) {
				s.draw(gl, mode);
			}
			
			/*
			 * ZOOM RECTANGLE
			 */
			if (currentMode.getActionCommand().equals("fill")) {
				double ratio;
				if (glPanel.getWidth() > glPanel.getHeight()) {
					ratio = glPanel.getWidth()/glPanel.getHeight();
				} else {
					ratio = glPanel.getHeight()/glPanel.getWidth();
				}
				
				gl.glBegin(GL.GL_LINE_LOOP);
				gl.glColor3f(0.0f, 0.0f, 0.0f);
				
				gl.glVertex3d((me.getX()-(0.25*ratio)*glPanel.getWidth()),(me.getY()-(0.25*glPanel.getHeight())),0.0f);
				gl.glVertex3d((me.getX()+(0.25*ratio)*glPanel.getWidth()),(me.getY()-(0.25*glPanel.getHeight())),0.0f);
				gl.glVertex3d((me.getX()+(0.25*ratio)*glPanel.getWidth()),(me.getY()+(0.25*glPanel.getHeight())),0.0f);
				gl.glVertex3d((me.getX()-(0.25*ratio)*glPanel.getWidth()),(me.getY()+(0.25*glPanel.getHeight())),0.0f);
				
				gl.glEnd();
			}
		} // drawAll
		
		@Override
		public void display(GLAutoDrawable glDrawable) {
			gl = glDrawable.getGL();
			gl.glClearColor(1.0f, 1.0f, 1.0f, 0f);
			gl.glClear(GL.GL_COLOR_BUFFER_BIT);
			gl.glMatrixMode(GL.GL_MODELVIEW);
		 	gl.glLoadIdentity();
			
			drawAll(gl, GL.GL_RENDER);
			
			if (BUTTON_DOWN) {
				draw(gl);
				BUTTON_DOWN = false;
			} else if (list.size() == 1 && currentMode.getActionCommand().equals("line")) {
				// SECOND VERTEX OF LINE OBJECT
				gl.glBegin(GL.GL_LINES);
				gl.glColor3f(currentColour.getBackground().getRed(), currentColour.getBackground().getGreen(), currentColour.getBackground().getBlue());
				gl.glVertex3f(list.get(0).x, list.get(0).y, 0f);
				gl.glVertex3f(me.getX(), me.getY(), 0f);
				gl.glEnd();
			} else if (list.size() == 1 && currentMode.getActionCommand().equals("square")) {
				// SECOND VERTEX OF SQUARE OBJECT
				gl.glBegin(GL.GL_POLYGON);
				gl.glColor3f(currentColour.getBackground().getRed(), currentColour.getBackground().getGreen(), currentColour.getBackground().getBlue());
				gl.glVertex3f(list.get(0).x, list.get(0).y, 0f);
				gl.glVertex3f(me.getX(), list.get(0).y, 0f);
				gl.glVertex3f(me.getX(), me.getY(), 0f);
				gl.glVertex3f(list.get(0).x, me.getY(), 0f);
				gl.glEnd();
			} else if (list.size() > 0 && currentMode.getActionCommand().equals("polygon")) {
				if (list.size() == 1) {
					gl.glBegin(GL.GL_LINES);
				} else {
					gl.glBegin(GL.GL_POLYGON);
				}
				gl.glColor3f(currentColour.getBackground().getRed(), currentColour.getBackground().getGreen(), currentColour.getBackground().getBlue());
				for (Coords c : list) {
					gl.glVertex3f(c.x, c.y, 0.0f);
				}
				gl.glVertex3f(me.getX(), me.getY(), 0f);
				gl.glEnd();
			} else if (list.size() == 1 && currentMode.getActionCommand().equals("circle")) {
				gl.glBegin( GL.GL_POLYGON );
				gl.glColor3f(currentColour.getBackground().getRed(), currentColour.getBackground().getGreen(), currentColour.getBackground().getBlue());
				float r = (float) Point2D.distance(list.getFirst().x, list.getFirst().y, me.getX(), me.getY());
				
				for( float angle = 0; angle < 2*Math.PI; angle += 0.01 ) {
					gl.glVertex3f( (float)(list.getFirst().x + r*Math.cos(angle)), (float)(list.getFirst().y + r*Math.sin(angle)), 0.0f );
				}
				
				gl.glEnd();
			}
			glDrawable.swapBuffers();
		}

		private int picker(GL gl) {
			int BUFSIZE = 512;
		    int[] selectBuf = new int[BUFSIZE];
		    IntBuffer selectBuffer = BufferUtil.newIntBuffer(BUFSIZE);
		    int hits;
		    int viewport[] = new int[4];

		    gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		    gl.glSelectBuffer(BUFSIZE, selectBuffer);
		    gl.glRenderMode(GL.GL_SELECT);
		    gl.glInitNames();
		    gl.glPushName(-1);
		    gl.glMatrixMode(GL.GL_PROJECTION);
		    gl.glPushMatrix();
		    gl.glLoadIdentity();
		    glu.gluPickMatrix((double) pickPoint.x, (double) (viewport[3] - pickPoint.y), 5.0, 5.0, viewport, 0);
		    gl.glOrtho(0.0, glPanel.getWidth(), glPanel.getHeight(), 0.0, -0.5, 2.5);
		    drawAll(gl, GL.GL_SELECT);
		    gl.glPopMatrix();
		    gl.glFlush();

		    hits = gl.glRenderMode(GL.GL_RENDER);
		    selectBuffer.get(selectBuf);
		    return processHits(hits, selectBuf);
		} // pick
		
		private int processHits(int hits, int buffer[]) {
		    int names;
		    int ptr = 0;

		    for (int i = 0; i < hits; i++) { 
		      names = buffer[ptr];
		      ptr = ptr + 3;
		      for (int j = 0; j < names; j++) {
		        ptr++;
		      }
		   }
		    if (hits == 0) {
		    	return -1;
		    } else {
		    	return buffer[ptr-1];
		    }
		 } // processHits
		
		@Override
		public void displayChanged(GLAutoDrawable glDrawable, boolean arg1, boolean arg2) {
			gl = glDrawable.getGL();
		}

		@Override
		public void init(GLAutoDrawable glDrawable) {
			originalWidth = glPanel.getWidth();
			originalHeight = glPanel.getHeight();
			
			gl = glDrawable.getGL();
			gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			gl.glViewport(0, 0, glPanel.getWidth(), glPanel.getHeight());
		    gl.glMatrixMode(GL.GL_PROJECTION);
		    gl.glLoadIdentity();
		    gl.glOrtho(0.0, glPanel.getWidth(), glPanel.getHeight(), 0.0, -0.5, 2.5);
		    gl.glMatrixMode(GL.GL_MODELVIEW);
		    gl.glLoadIdentity();
		}

		@Override
		public void reshape(GLAutoDrawable glDrawable, int arg1, int arg2, int arg3, int arg4) {
			originalWidth = glPanel.getWidth();
			originalHeight = glPanel.getHeight();
			
			gl = glDrawable.getGL();
			gl.glViewport(0, 0, glPanel.getWidth(), glPanel.getHeight());
		    gl.glMatrixMode(GL.GL_PROJECTION);
		    gl.glLoadIdentity();
		    gl.glOrtho(0.0, glPanel.getWidth(), glPanel.getHeight(), 0.0, -0.5, 2.5);
		    gl.glMatrixMode(GL.GL_MODELVIEW);
		    gl.glLoadIdentity();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			if (currentMode.getActionCommand().equals("pointer")) {
				//showColour(e);
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			coords.setText("");
		}

		@Override
		public void mousePressed(MouseEvent e) {
			BUTTON_DOWN = true;
			me = e;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			coords.setText(e.getX() + ", " + e.getY());
			me = e;
			pickPoint = e.getPoint();
			
		} // mouseMoved
				
	} // Renderer
	
	private class CustomColourListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			Color newColour = tcc.getColor();
			currentColour.setBackground(newColour);
		}
		
	}
	
	private class ModeSelectListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			currentMode.setBorder(BorderFactory.createRaisedBevelBorder());
			currentMode = (JButton)event.getSource();
			currentMode.setBorder(BorderFactory.createLoweredBevelBorder());
			buttonPanel.repaint();
			
			if (event.getActionCommand().equals("pointer")) {
				
			}
		}
		
	}
	
	private class ButtonListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			
			if (event.getActionCommand().equals("Exit")) {
				System.exit(0);
			} else if (event.getActionCommand().equals("cColour")) {
				Color newColour = JColorChooser.showDialog(
							                        painter,
							                        "Choose Custom Color",
							                        currentColour.getBackground());
				if (newColour != null) {
					currentColour.setBackground(newColour);
				}

			} else if (event.getActionCommand().equals("Save")) {
				/*GL glTemp = rend.gl;
				//ByteBuffer b = BufferUtil.newByteBuffer(glPanel.getWidth()*glPanel.getHeight()*3);
				//IntBuffer b = ByteBuffer.allocateDirect((glPanel.getWidth()*glPanel.getHeight())<<2).order(ByteOrder.nativeOrder()).asIntBuffer();
				//glTemp.glReadPixels(0, 0, glPanel.getWidth(), glPanel.getHeight(), GL.GL_BGRA, GL.GL_UNSIGNED_BYTE, b);
				
				pixelRGB = ByteBuffer.allocateDirect(glPanel.getWidth()*glPanel.getHeight() * 3);
				glTemp.glReadPixels(0, 0, glPanel.getX(), glPanel.getY(), GL.GL_RGB, GL.GL_UNSIGNED_BYTE, pixelRGB);
				
				BufferedImage img = new BufferedImage(glPanel.getWidth(), glPanel.getHeight(), BufferedImage.TYPE_INT_RGB); 
		        for (int x = 0; x < glPanel.getWidth(); x++) { 
		        	for (int y = 0; y < glPanel.getHeight(); y++) { 
		        		img.setRGB (x, y, pixelRGB.get((glPanel.getHeight() - y - 1) * glPanel.getWidth() + x)); 
		            } 
		        }
		        try {
		        	OutputStream out = new BufferedOutputStream(new FileOutputStream("glPaint.jpg"));
		    		ImageIO.write(img, "jpeg", out);
		        } catch (IOException e) {
					e.printStackTrace();
				}*/
			}
		}
	}
	
	private class ColourListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			currentColour.setBackground(new Color(Integer.parseInt(event.getActionCommand())));
		}
	}
	
	private class customUI extends MetalToggleButtonUI {
		public Color getSelectColor() {
			return Color.GRAY;
		}
	}
	
	

	@Override
	public void componentHidden(ComponentEvent arg0) {
		
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		int width = getWidth();
        int height = getHeight();
        
        boolean resize = false;
	    if (width < MIN_WIDTH) {
	         resize = true;
	         width = MIN_WIDTH;
	    }
	    if (height < MIN_HEIGHT) {
	         resize = true;
	         height = MIN_HEIGHT;
	    }
	    if (width > 990) {
	    	resize = true;
	        width = 990;
	    }
	    if (height > 750) {
	    	resize = true;
	        height = 750;
	    }
        if (resize) {
              setSize(width, height);
        }
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		
	}

}
