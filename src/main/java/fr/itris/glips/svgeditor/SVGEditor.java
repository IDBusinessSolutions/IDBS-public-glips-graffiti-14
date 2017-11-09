/*
 * Created on 23 mars 2004 
 * 
 =============================================
 GNU LESSER GENERAL PUBLIC LICENSE Version 2.1
 =============================================
 GLIPS Graffiti Editor, a SVG Editor
 Copyright (C) 2003 Jordi SUC, Philippe Gil, SARL ITRIS
 
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
 Contact : jordi.suc@itris.fr; philippe.gil@itris.fr
 
 =============================================
 */
package fr.itris.glips.svgeditor;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.image.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.text.*;
import java.util.*;

import javax.swing.*;

import org.w3c.dom.*;

import fr.itris.glips.svgeditor.canvas.*;
import fr.itris.glips.svgeditor.colorchooser.*;
import fr.itris.glips.svgeditor.menutool.*;
import fr.itris.glips.svgeditor.resources.*;
import fr.itris.glips.svgeditor.selection.*;
import fr.itris.glips.svgeditor.shape.*;
import fr.itris.glips.svgeditor.undoredo.*;

/**
 * The main class of the editor
 * 
 * @author Jordi SUC
 * @author Alastair Dant / IDBS
 */
public class SVGEditor
{
    /**
     * whether the editor handles the rtda animations
     */
    public static boolean isRtdaAnimationsVersion = false;

    /**
     * the parent container
     */
    private Container parentContainer = null;

    /**
     * the component in which SVGFrames are inserted
     */
    private JComponent desktop;

    /**
     * the module loader
     */
    private SVGModuleManager moduleManager;

    /**
     * the class that gives the resources
     */
    private SVGResource resource;

    /**
     * the toolkit object
     */
    private SVGToolkit toolkit;

    /**
     * tells whether the mutli windowing method has to be used or not
     */
    private boolean isMultiWindow = false;

    /**
     * the undo/Rdo module
     */
    private SVGUndoRedo undoRedo;

    /**
     * the map associating a name to a rectangle representing bounds
     */
    private final Map<String, Rectangle> widgetBounds = new Hashtable<String, Rectangle>();

    /**
     * the selection module
     */
    private SVGSelection selection;

    /**
     * the drag source
     */
    private DragSource dragSource = DragSource.getDefaultDragSource();

    /**
     * the color chooser of the editor
     */
    private static SVGColorChooser colorChooser = null;

    /**
     * whether the quit action is disabled
     */
    private boolean isQuitActionDisabled = false;

    /**
     * whether the JVM will be exited when the user requires to exit from the
     * editor
     */
    private boolean canExitFromJVM = false;

    /**
     * the font
     */
    public static final Font theFont = new Font("theFont", Font.ROMAN_BASELINE, 12); //$NON-NLS-1$

    /**
     * the small font
     */
    public static final Font smallFont = new Font("smallFont", Font.ROMAN_BASELINE, 10); //$NON-NLS-1$

    /**
     * the set of the runnables that should be run when the editor is exiting
     */
    private HashSet<Runnable> disposeRunnables = new HashSet<Runnable>();

    /**
     * the map of the name spaces that should be checked
     */
    public static HashMap<String, String> requiredNameSpaces = new HashMap<String, String>();

    /**
     * the resource bundle for this editor
     */
    private static ResourceBundle bundle = null;

    /**
     * the decimal format and the format used when displaying numbers
     */
    private static DecimalFormat format, displayFormat;


    /**
     * code to be executed whenever a file is saved successfully
     */
    private Runnable runOnSave = null;    
    
    /**
     * creating the bundle
     */
    static
    {
        try
        {
            bundle = ResourceBundle.getBundle("fr.itris.glips.svgeditor.resources.properties.SVGEditorStrings"); //$NON-NLS-1$
        }
        catch (Exception ex)
        {
            bundle = null;
        }

        // sets the format object that will be used to convert a double value
        // into a string
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        format = new DecimalFormat("############.################", symbols); //$NON-NLS-1$
        displayFormat = new DecimalFormat("##########.#", symbols); //$NON-NLS-1$
    }

    /**
     * The constructor of the class
     */
    public SVGEditor()
    {
    }

    /**
     * initializing the editor
     * 
     * @param parent
     *            the parent container for the application
     * @param fileToBeLoaded
     *            the file to be directly loaded
     * @param showSplash
     *            whether the splash screen should be shown or not
     * @param displayFrame
     *            whether or not to show the frame
     * @param quitDisabled
     *            whether the quit action is disabled
     * @param exitFromJVM
     *            whether the JVM will be exited when the user requires to exit
     *            from the editor
     * @param disposeRunnable
     *            the runnable that should be run when exiting the editor
     */
    public void init(Container parent, String fileToBeLoaded, boolean showSplash, final boolean displayFrame,
            boolean quitDisabled, boolean exitFromJVM, Runnable disposeRunnable)
    {
        parentContainer = parent;
        this.isQuitActionDisabled = quitDisabled;
        this.canExitFromJVM = exitFromJVM;

        if (disposeRunnable != null)
        {
            this.disposeRunnables.add(disposeRunnable);
        }

        // setting the values for the tooltip manager
        ToolTipManager.sharedInstance().setInitialDelay(100);
        ToolTipManager.sharedInstance().setDismissDelay(10000);
        ToolTipManager.sharedInstance().setReshowDelay(100);

        // the window containing the splash screen
        JWindow window = null;

        if (showSplash)
        {
            // the icon that will be displayed in the splash screen
            final ImageIcon icon = SVGResource.getIcon("Splash", false); //$NON-NLS-1$
            window = new JWindow();
            final int mww = icon.getIconWidth(), mwh = icon.getIconHeight();
            window.setBounds((int)(Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2 - mww / 2), (int)(Toolkit
                    .getDefaultToolkit().getScreenSize().getHeight() / 2 - mwh / 2), mww, mwh);
            final BufferedImage splashImage = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
            splashImage.getGraphics().drawImage(icon.getImage(), 0, 0, window);

            // the panel displaying the image
            JPanel panel = new JPanel()
            {
                @Override
                protected void paintComponent(Graphics g)
                {
                    super.paintComponent(g);

                    if (icon.getImage() != null)
                    {

                        ((Graphics2D)g).drawRenderedImage(splashImage, null);
                    }
                }
            };

            window.getContentPane().setLayout(new BoxLayout(window.getContentPane(), BoxLayout.Y_AXIS));
            window.getContentPane().add(panel);
            window.setVisible(true);
        }

        // creating the toolkit object
        toolkit = new SVGToolkit(this);

        // creating the resource manager
        resource = new SVGResource(this);

        // getting the configuration values

        // the bounds of the widgets contained in the main frame
        Map<String, Rectangle> map = getWidgetBoundsMap();

        if (map != null)
        {
            widgetBounds.putAll(map);
        }

        // gets the type of the desktop
        String display = getWindowDisplayType();

        if (display != null)
        {
            if (display.equals("multi-window")) //$NON-NLS-1$
            {
                isMultiWindow = true;

                // the component into which all the SVGFrames containing the
                // scroll panes will be placed
                desktop = new JDesktopPane();
                ((JDesktopPane)desktop).setDragMode(JDesktopPane.LIVE_DRAG_MODE);
            }
        }

        if (desktop == null)
        {
            // the desktop is not multi-windowed
            desktop = new JPanel();
            desktop.setLayout(new BoxLayout(desktop, BoxLayout.X_AXIS));
        }

        // the module loader is created and initialized
        moduleManager = new SVGModuleManager(this);
        moduleManager.init();

        // sets the default color chooser
        if (getColorChooser() == null)
        {
            setColorChooser(new SVGColorChooser(this));
        }

        // sets particular modules
        try
        {
            undoRedo = (SVGUndoRedo)moduleManager.getModule("UndoRedo"); //$NON-NLS-1$
        }
        catch (Exception ex)
        {
            undoRedo = null;
        }

        try
        {
            selection = (SVGSelection)moduleManager.getModule("Selection"); //$NON-NLS-1$
        }
        catch (Exception ex)
        {
            selection = null;
        }

        if (parentContainer instanceof JFrame)
        {
            // setting the icon
            ImageIcon icon2 = SVGResource.getIcon("Editor", false); //$NON-NLS-1$

            if (icon2 != null && icon2.getImage() != null)
            {
                ((JFrame)parentContainer).setIconImage(icon2.getImage());
            }

            // handling the frame content
            ((JFrame)parentContainer).getContentPane().setLayout(new BorderLayout());
            ((JFrame)parentContainer).getContentPane().add(moduleManager.getEditorToolBars(), BorderLayout.NORTH);
            ((JFrame)parentContainer).getContentPane().add(desktop, BorderLayout.CENTER);
            ((JFrame)parentContainer).setJMenuBar(moduleManager.getMenuBar());

            // computing the bounds of the main frame
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(
                ((JFrame)parentContainer).getGraphicsConfiguration());
            final Rectangle frameBounds = new Rectangle(screenInsets.left, screenInsets.top, screenSize.width
                - screenInsets.left - screenInsets.right, screenSize.height - screenInsets.top - screenInsets.bottom);
            widgetBounds.put("mainframe", frameBounds); //$NON-NLS-1$

            // sets the bounds of the main frame
            ((JFrame)parentContainer).setBounds(frameBounds);
            desktop.setBounds(frameBounds);
        }
        else if (parentContainer instanceof JApplet)
        {
            // handling the applet content
            ((JApplet)parentContainer).getContentPane().setLayout(
                new BoxLayout(((JFrame)parentContainer).getContentPane(), BoxLayout.Y_AXIS));
            ((JApplet)parentContainer).getContentPane().add(moduleManager.getEditorToolBars(), BorderLayout.NORTH);
            ((JApplet)parentContainer).getContentPane().add(desktop, BorderLayout.CENTER);
            ((JApplet)parentContainer).setJMenuBar(moduleManager.getMenuBar());
        }

        // layout some elements inside the frame
        moduleManager.layoutElements();

        // displays the main frame
        if (displayFrame && parentContainer instanceof JFrame)
        {
            ((JFrame)parentContainer).setVisible(true);
        }

        // opening the file specified in the constructor arguments
        openFile(fileToBeLoaded);

        if (window != null)
        {
            window.setVisible(false);
        }
    }

    /**
     * @return the resource bundle
     */
    public static ResourceBundle getBundle()
    {
        return bundle;
    }

    /**
     * @return the class that manages the different frames in the desktop pane
     */
    public SVGFrameManager getFrameManager()
    {
        return moduleManager.getFrameManager();
    }

    /**
     * @return the main frame
     */
    public Container getParent()
    {
        return parentContainer;
    }

    /**
     * shows or hides the editor frame
     * 
     * @param visible
     *            whether the editor frame should be visible or not
     */
    public void setVisible(final boolean visible)
    {
        // invoking the given runnable in the AWT thread
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                if (parentContainer instanceof JFrame)
                {
                    ((JFrame)parentContainer).setVisible(visible);
                    ((JFrame)parentContainer).toFront();

                    if (!visible)
                    {
                        closeAll();
                    }
                }
            }
        });
    }

    /**
     * @return Returns the isQuitActionDisabled.
     */
    public boolean isQuitActionDisabled()
    {
        return isQuitActionDisabled;
    }

    /**
     * @return the boolean telling whether the mutli windowing method has to be
     *         used or not
     */
    public boolean isMultiWindow()
    {
        return isMultiWindow;
    }

    /**
     * @return Returns the dragSource.
     */
    public DragSource getDragSource()
    {
        return dragSource;
    }

    /**
     * @return Returns the colorChooser.
     */
    public static SVGColorChooser getColorChooser()
    {
        return colorChooser;
    }

    /**
     * sets the new color chooser
     * 
     * @param newColorChooser
     *            The colorChooser to set.
     */
    public static void setColorChooser(SVGColorChooser newColorChooser)
    {
        colorChooser = newColorChooser;
    }

    /**
     * @return the map containing the bounds of each widget in the main frame
     */
    protected Map<String, Rectangle> getWidgetBoundsMap()
    {
        Map<String, Rectangle> map = new Hashtable<String, Rectangle>();
        Document doc = SVGResource.getXMLDocument("config.xml"); //$NON-NLS-1$

        if (doc != null)
        {
            Element root = doc.getDocumentElement();

            if (root != null)
            {
                // getting the node containing the nodes giving the bounds of
                // the widgets in the main frame
                Node cur = null, bounds = null;

                for (cur = root.getFirstChild(); cur != null; cur = cur.getNextSibling())
                {
                    if (cur instanceof Element && cur.getNodeName().equals("bounds")) //$NON-NLS-1$
                    {
                        bounds = cur;
                        break;
                    }
                }

                if (bounds != null)
                {
                    // filling the map with the bounds
                    Rectangle rectBounds = null;
                    int x = 0, y = 0, width = 0, height = 0;
                    String name, strX, strY, strW, strH;
                    Element el = null;

                    for (cur = bounds.getFirstChild(); cur != null; cur = cur.getNextSibling())
                    {
                        if (cur instanceof Element && cur.getNodeName().equals("widget")) //$NON-NLS-1$
                        {
                            el = (Element)cur;

                            // the name of the widget
                            name = el.getAttribute("name"); //$NON-NLS-1$

                            // getting each value of the bounds
                            strX = el.getAttribute("x"); //$NON-NLS-1$
                            strY = el.getAttribute("y"); //$NON-NLS-1$
                            strW = el.getAttribute("width"); //$NON-NLS-1$
                            strH = el.getAttribute("height"); //$NON-NLS-1$

                            x = 0;
                            y = 0;
                            width = 0;
                            height = 0;

                            try
                            {
                                x = Integer.parseInt(strX);
                                y = Integer.parseInt(strY);
                                width = Integer.parseInt(strW);
                                height = Integer.parseInt(strH);
                            }
                            catch (Exception ex)
                            {
                            }

                            // creating the rectangle
                            rectBounds = new Rectangle(x, y, width, height);

                            // putting the bounds in the map
                            if (name != null && !name.equals("")) //$NON-NLS-1$
                            {

                                map.put(name, rectBounds);
                            }
                        }
                    }
                }
            }
        }

        return map;
    }

    /**
     * @return the display type
     */
    protected String getWindowDisplayType()
    {
        String display = ""; //$NON-NLS-1$

        Document doc = SVGResource.getXMLDocument("config.xml"); //$NON-NLS-1$

        if (doc != null)
        {
            Element root = doc.getDocumentElement();

            if (root != null)
            {
                // getting the node containing the nodes giving the bounds of
                // the widgets in the main frame
                Node cur = null, displayNode = null;

                for (cur = root.getFirstChild(); cur != null; cur = cur.getNextSibling())
                {

                    if (cur instanceof Element && cur.getNodeName().equals("display")) //$NON-NLS-1$
                    {
                        displayNode = cur;
                        break;
                    }
                }

                if (displayNode != null)
                {
                    display = ((Element)cur).getAttribute("type"); //$NON-NLS-1$
                }
            }
        }

        return display;
    }

    /**
     * closes all the opened svg files
     */
    public void closeAll()
    {
        Object obj = null;

        if (getSVGModuleLoader() != null)
        {
            obj = getSVGModuleLoader().getModule("SaveClose"); //$NON-NLS-1$
        }

        if (obj != null)
        {
            Class[] cargs = {Runnable.class};
            Object[] args = {runOnSave};

            try
            {
                obj.getClass().getMethod("closeAllAction", cargs).invoke(obj, args); //$NON-NLS-1$
            }
            catch (Exception ex)
            {
            }
        }
    }

    /**
     * saves the file of the given frame
     * 
     * @param frame
     *            a frame
     */
    public void saveFrame(SVGFrame frame)
    {
        if (frame != null)
        {
            // getting the save module
            Object obj = null;

            if (getSVGModuleLoader() != null)
            {
                obj = getSVGModuleLoader().getModule("SaveClose"); //$NON-NLS-1$
            }

            if (obj != null)
            {
                // invoke the save action via reflection for some mad reason
                Class[] cargs =
                { SVGFrame.class, boolean.class, boolean.class, Runnable.class };
                Object[] args =
                { frame, false, false, runOnSave};

                try
                {
                    obj.getClass().getMethod("saveAction", cargs).invoke(obj, args); //$NON-NLS-1$
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * opens the
     * 
     * @param path
     */
    public void openFile(final String path)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                if (path != null && !path.equals("")) //$NON-NLS-1$
                {
                    setVisible(true);

                    Object obj = moduleManager.getModule("NewOpen"); //$NON-NLS-1$
                    File file = null;

                    try
                    {
                        file = new File(new URI(path));
                    }
                    catch (Exception ex)
                    {
                    }

                    if (obj != null)
                    {
                        Class[] cargs =
                        { File.class };
                        Object[] args =
                        { file };
                        try
                        {
                            obj.getClass().getMethod("open", cargs).invoke(obj, args); //$NON-NLS-1$
                        }
                        catch (Exception e)
                        {
                        }
                    }
                }
            }
        });
    }

    /**
     * @param name
     *            the name of a widget
     * @return the preferred bounds of a widget
     */
    public Rectangle getPreferredWidgetBounds(String name)
    {
        Rectangle rect = null;

        if (name != null && !name.equals("")) //$NON-NLS-1$
        {
            try
            {
                rect = widgetBounds.get(name);
            }
            catch (Exception ex)
            {
                rect = null;
            }
        }

        return rect;
    }

    /**
     * @return the component into which all the panels containing the
     *         JScrollPanes will be placed
     */
    public JComponent getDesktop()
    {
        return desktop;
    }

    /**
     * @return the menubar
     */
    public SVGMenuBar getMenuBar()
    {
        return moduleManager.getMenuBar();
    }

    /**
     * @return the tool bar
     */
    public SVGEditorToolbars getEditorToolBars()
    {
        return moduleManager.getEditorToolBars();
    }

    /**
     * @return the popup manager
     */
    public SVGPopup getPopupManager()
    {
        return moduleManager.getPopupManager();
    }

    /**
     * @return Returns the resourceImageManager.
     */
    public SVGResourceImageManager getResourceImageManager()
    {
        return moduleManager.getResourceImageManager();
    }

    /**
     * @return the module loader
     */
    public SVGModuleManager getSVGModuleLoader()
    {
        return moduleManager;
    }

    /**
     * @return the toolkit object containing utility methods
     */
    public SVGToolkit getSVGToolkit()
    {
        return toolkit;
    }

    /**
     * @return the painter manager
     */
    public SVGColorManager getSVGColorManager()
    {
        return moduleManager.getColorManager();
    }

    /**
     * @return the manager of the cursors
     */
    public SVGCursors getCursors()
    {
        return moduleManager.getCursors();
    }

    /**
     * @return an object of the class managing the resources
     */
    public SVGResource getResource()
    {
        return resource;
    }

    /**
     * used to call the same method on each module
     * 
     * @param method
     *            the method name
     * @param cargs
     *            an array of Class objects
     * @param args
     *            an array of objects
     */
    public void invokeOnModules(String method, Class[] cargs, Object[] args)
    {
        Collection modules = moduleManager.getModules();
        Iterator it = modules.iterator();
        Object current = null;

        while (it.hasNext())
        {

            current = it.next();
            try
            {
                Method meth = current.getClass().getMethod(method, cargs);
                // invokes the method on each instance contained in the map
                meth.invoke(current, args);
            }
            catch (Exception ex)
            {
            }
        }
    }

    /**
     * @return the collection of the shape modules
     */
    public Collection getShapeModules()
    {
        return moduleManager.getShapeModules();
    }

    /**
     * cancels all the actions that could be running and enables the regular
     * selection mode if the boolean is true, disables it otherwise
     * 
     * @param enableRegularMode
     *            true to enable the regular selection mode
     */
    public void cancelActions(boolean enableRegularMode)
    {
        // invokes the "cancelActions" on each module
        Collection modules = getSVGModuleLoader().getModules();
        SVGModule module = null;

        for (Iterator it = modules.iterator(); it.hasNext();)
        {
            try
            {
                module = (SVGModule)it.next();
            }
            catch (Exception ex)
            {
                module = null;
            }

            if (module != null)
            {
                module.cancelActions();
            }
        }

        if (getSVGSelection() != null)
        {
            getSVGSelection().setSelectionEnabled(enableRegularMode);
        }

        if (isMultiWindow())
        {
            getEditorToolBars().cancelActions();
        }
    }

    /**
     * @param name
     *            the name of the module
     * @return the module object
     */
    public Object getModule(String name)
    {
        return moduleManager.getModule(name);
    }

    /**
     * @param name
     *            the name of the shape module
     * @return the shape module object
     */
    public SVGShape getShapeModule(String name)
    {
        return moduleManager.getShapeModule(name);
    }

    /**
     * @return the static undoRedo module
     */
    public SVGUndoRedo getUndoRedo()
    {
        return undoRedo;
    }

    /**
     * @return the static selection module
     */
    public SVGSelection getSVGSelection()
    {
        return selection;
    }

    /**
     * @return Returns the format.
     */
    public static DecimalFormat getFormat()
    {
        return format;
    }

    /**
     * @return the format used when numbers have to be displayed
     */
    public static DecimalFormat getDisplayFormat()
    {
        return displayFormat;
    }

    /**
     * exits the editor
     */
    public void exit()
    {
        // saving the current state of the editor
        getResource().saveEditorsCurrentState();

        if (isQuitActionDisabled())
        {
            // hiding the editor's frame
            setVisible(false);
        }
        else
        {
            // quitting the editor
            // checks if some svg documents have been modified
            boolean displayDialog = false;
            Collection<SVGFrame> frames = new LinkedList<SVGFrame>(getFrameManager().getFrames());

            if (frames != null && frames.size() > 0)
            {
                for (SVGFrame frm : frames)
                {
                    if (frm != null && frm.isModified())
                    {
                        displayDialog = true;
                        break;
                    }
                }
            }

            boolean canExitEditor = canExitFromJVM;

            // if svg documents have been modified, display an alert dialog
            if (displayDialog)
            {
                String messageexit = "", titleexit = ""; //$NON-NLS-1$ //$NON-NLS-2$

                if (bundle != null)
                {
                    try
                    {
                        messageexit = bundle.getString("messageexit"); //$NON-NLS-1$
                        titleexit = bundle.getString("titleexit"); //$NON-NLS-1$
                    }
                    catch (Exception ex)
                    {
                    }
                }

                int returnVal = JOptionPane.showConfirmDialog(getParent(), messageexit, titleexit,
                    JOptionPane.YES_NO_OPTION);
                canExitEditor = canExitEditor && returnVal == JOptionPane.YES_OPTION;
            }

            if (canExitEditor)
            {
                // running the dispose runnables
                for (Runnable runnable : disposeRunnables)
                {
                    runnable.run();
                }               
            }
        }
    }
    
    /**
     * @param runOnSave Code to be run whenever a file is saved successfully
     */
    public void setSaveCompletedHandler(Runnable runOnSave)
    {
        this.runOnSave = runOnSave;
    }  
}
