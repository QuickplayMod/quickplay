package co.bugg.quickplay.client.gui.config;

import co.bugg.quickplay.Quickplay;
import co.bugg.quickplay.Reference;
import co.bugg.quickplay.client.QuickplayColor;
import co.bugg.quickplay.client.gui.QuickplayGui;
import co.bugg.quickplay.client.gui.components.QuickplayGuiButton;
import co.bugg.quickplay.client.gui.components.QuickplayGuiComponent;
import co.bugg.quickplay.client.gui.components.QuickplayGuiSlider;
import co.bugg.quickplay.client.gui.components.QuickplayGuiString;
import co.bugg.quickplay.config.AConfiguration;
import co.bugg.quickplay.config.AssetFactory;
import co.bugg.quickplay.config.GuiOption;
import co.bugg.quickplay.util.Message;
import net.minecraft.client.gui.GuiPageButtonList;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Basic GUI for editing any {@link AConfiguration}
 * Many {@link AConfiguration}s use their own GUI/customization system,
 * but they are capable of using this system if desired.
 */
public class QuickplayGuiEditConfig extends QuickplayGui {
    /**
     * {@link AConfiguration} this GUI is editing
     */
    public AConfiguration config;
    /**
     * A list of elements this GUI is handling
     * Taken directly from {@link #config} in {@link #initGui()}
     */
    public List<ConfigElement> configElements = new ArrayList<>();

    /**
     * Constructor
     *
     * @param config The {@link AConfiguration} this GUI is editing
     */
    public QuickplayGuiEditConfig(AConfiguration config) {
        this.config = config;
        Quickplay.INSTANCE.registerEventHandler(this);
    }

    /**
     * The scale of the header at the top of the screen
     * Set in {@link #initGui()}
     * Responsive
     */
    public double headerScale;
    /**
     * The scale of the subheader below the header
     * Set in {@link #initGui()}
     * Responsive
     */
    public double subheaderScale;
    /**
     * Y location of the subheader
     */
    public int subheaderY;
    /**
     * Margins of the gray background box
     * Percentage-based, e.g. 0.1 or 0.2
     * Multiplied by two to account for both sides of the box
     */
    public double boxMargins;
    /**
     * Y level that is the top of the background box
     */
    public int topOfBox;
    /**
     * How wide the background box is (e.g. width - boxMargins * 2)
     */
    public int boxWidth;
    /**
     * How high the background box is (e.g. height - topOfBox)
     */
    public int boxHeight;
    /**
     * The size of each element in the config, including margins
     */
    public int elementSize;
    /**
     * The distance vertically elements must move to go from opacity 0 to opacity 1 at the fade line
     */
    final int fadeDistance = 10;
    /**
     * Last two logged Y positions of the mouse on the screen
     * This is used for hovering tooltips, as you must hover for a certain
     * period of time before the tooltip appears
     */
    public int[] lastTwoMouseX = new int[2];
    /**
     * Last two logged X positions of the mouse on the screen
     * This is used for hovering tooltips, as you must hover for a certain
     * period of time before the tooltip appears
     */
    public int[] lastTwoMouseY = new int[2];
    /**
     * How many game ticks the mouse has been standing still (within the provided margin)
     */
    public int mouseStandStillTicks = 0;
    /**
     * How many ticks the mouse must be standing still for the tooltip to appear
     */
    public final int hoverDelayTicks = 10;
    /**
     * How many pixels the mouse may move each tick before {@link #mouseStandStillTicks} is invalidated
     */
    public final int mouseStandStillMargin = 2;
    /**
     * How wide the button to open the Quickplay configuration folder iis
     */
    public final int openFolderButtonWidth = 90;
    /**
     * The margins around the Quickplay configuration folder button
     */
    public final int openFolderButtonMargins = 4;
    /**
     * The text displayed on the open folder button
     */
    public final String openFolderText = I18n.format("quickplay.config.openfolder");

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Quickplay.INSTANCE.unregisterEventHandler(this);
    }

    @Override
    public void initGui() {
        configElements.clear();
        super.initGui();

        /*
         * Calculate various sizes and positions
         */

        // Header size is responsive to screen size
        headerScale = height > 400 ? 2 : 1.5;
        subheaderScale = height > 400 ? 1.3 : 1;

        subheaderY = // Subheader should be 3 pixels below main header
                (int) (height * 0.05 / subheaderScale) + (int) (fontRenderer.FONT_HEIGHT * headerScale) + (int) (3 / headerScale);

        // Padding on the sides of the list (responsive)
        boxMargins = width < 500 ? 0.1 : 0.2;
        // +20 to the top because for some reason subheaderY + subheader height isn't actually the bottom of the subheader... fix
        topOfBox = (int) (subheaderY + fontRenderer.FONT_HEIGHT * subheaderScale + 20);

        boxWidth = (int) (width * (1 - (boxMargins * 2)));
        boxHeight = height - topOfBox;

        elementSize = (ConfigElement.ELEMENT_HEIGHT + ConfigElement.ELEMENT_MARGINS);

        /*
         * Get the config elements that can be changed
         */
        Field[] fields = config.getClass().getDeclaredFields();
        for(Field field : fields) {
            field.setAccessible(true);
            GuiOption guiOptionDisplay = field.getAnnotation(GuiOption.class);
            if(guiOptionDisplay != null) {
                try {
                    configElements.add(new ConfigElement(field.get(config), guiOptionDisplay, I18n.format(field.getName())));
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    e.printStackTrace();
                    Quickplay.INSTANCE.sendExceptionRequest(e);
                }
            }
        }

        /*
         * Sort elements
         */
        configElements.sort(Comparator.comparing(o -> I18n.format(o.optionInfo.category())));

        /*
         * Create the necessary buttons
         */

        int nextButtonId = 0;
        // Get the width of each button
        int buttonWidth = 200;
        if(boxWidth < 200 + ConfigElement.ELEMENT_MARGINS * 2)
            // If scroll bar is being drawn, buttons should be moved over a lil bit to give it room
            buttonWidth = boxWidth - ConfigElement.ELEMENT_MARGINS * 2 - ((scrollFrameBottom - scrollFrameTop) / scrollContentHeight < 1 ? scrollbarWidth + ConfigElement.ELEMENT_MARGINS : 0);

        // These objects help format & handle changes to sliders, text boxes, and boolean boxes
        ConfigGuiResponder guiResponder = new ConfigGuiResponder();
        SliderFormatHelper formatHelper = new SliderFormatHelper();

        String previousCategory = null;

        for(ConfigElement element : configElements) {
            if(previousCategory == null || !previousCategory.equals(I18n.format(element.optionInfo.category()))) {
                componentList.add(new QuickplayGuiString(null, nextButtonId, width / 2, getElementY(nextButtonId) + ConfigElement.ELEMENT_HEIGHT - ConfigElement.ELEMENT_MARGINS - mc.fontRenderer.FONT_HEIGHT, buttonWidth, ConfigElement.ELEMENT_HEIGHT, I18n.format(element.optionInfo.category()), true, true));
                nextButtonId++;
            }
            previousCategory = I18n.format(element.optionInfo.category());

            int buttonX = width / 2 - (ConfigElement.ELEMENT_MARGINS + buttonWidth) / 2;
            int buttonY = getElementY(nextButtonId);

            // Figure out what button type needs to be rendered & give it the appropriate text
            if(element.element instanceof Boolean)
                componentList.add(new QuickplayGuiButton(element, nextButtonId, buttonX, buttonY, buttonWidth, ConfigElement.ELEMENT_HEIGHT, I18n.format(element.optionInfo.name()) + ": " + I18n.format((boolean) element.element ? "quickplay.config.gui.true" : "quickplay.config.gui.false"), true));
            else if(element.element instanceof QuickplayColor || element.element instanceof Runnable)
                componentList.add(new QuickplayGuiButton(element, nextButtonId, buttonX, buttonY, buttonWidth, ConfigElement.ELEMENT_HEIGHT, I18n.format(element.optionInfo.name()), true));
            else if(element.element instanceof Double)
                componentList.add(new QuickplayGuiSlider(guiResponder, element, nextButtonId, buttonX, buttonY, buttonWidth, ConfigElement.ELEMENT_HEIGHT, I18n.format(element.optionInfo.name()), element.optionInfo.minValue(), element.optionInfo.maxValue(), ((Number) element.element).floatValue(), formatHelper, true));
            else if(element.element.getClass().isEnum())
                componentList.add(new QuickplayGuiButton(element, nextButtonId, buttonX, buttonY, buttonWidth, ConfigElement.ELEMENT_HEIGHT, I18n.format(element.optionInfo.name()) + ": " + I18n.format(String.valueOf(element.element)), true));

            nextButtonId++;
        }

        componentList.add(new QuickplayGuiButton(null, nextButtonId, width - openFolderButtonMargins - openFolderButtonWidth, openFolderButtonMargins, openFolderButtonWidth, 20, openFolderText, false));

        setScrollingValues();
    }

    @Override
    public void setScrollingValues() {
        super.setScrollingValues();
        scrollFrameTop = topOfBox;
        scrollFrameBottom = height - 4;
        scrollbarYMargins = 2;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        /*
         * Calculate stuff necessary for whether hover text should be displayed
         */

        // Add the current mouseX and mouseY to lastTwoMouse arrays
        lastTwoMouseX[1] = lastTwoMouseX[0];
        lastTwoMouseX[0] = mouseX;
        lastTwoMouseY[1] = lastTwoMouseY[0];
        lastTwoMouseY[0] = mouseY;

        // If the mouse moved enough within the last two frames then reset stand still length
        if(Math.abs(lastTwoMouseX[0] - lastTwoMouseX[1]) >= mouseStandStillMargin || Math.abs(lastTwoMouseY[0] - lastTwoMouseY[1]) >= mouseStandStillMargin)
            mouseStandStillTicks = 0;

        // Blend is enabled for the GUI fadein
        // Fade in opacity has to be applied individually to each component that you want to fade in
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);

        /*
         * Draw background
         */

        drawDefaultBackground();

        /*
         * Draw the header text
         */

        if(opacity > 0) {
            // Scale up to header size
            GL11.glScaled(headerScale, headerScale, headerScale);
            drawCenteredString(fontRenderer, I18n.format("quickplay.config.gui.title"), (int) (width / 2 / headerScale), (int) (height * 0.05 / headerScale),
                    // Replace the first 8 bits (built-in alpha) with the custom fade-in alpha
                    (Quickplay.INSTANCE.settings.primaryColor.getColor().getRGB() & 0xFFFFFF) | ((int) (opacity * 255) << 24));
            // Scale back down
            GL11.glScaled(1 / headerScale, 1 / headerScale, 1 / headerScale);

            // Scale up to subheader size
            GL11.glScaled(subheaderScale, subheaderScale, subheaderScale);
            drawCenteredString(fontRenderer, I18n.format("quickplay.config.gui.version") + " " + Reference.VERSION, (int) (width / 2 / subheaderScale),
                    subheaderY,
                    // Replace the first 8 bits (built-in alpha) with the custom fade-in alpha
                    (Quickplay.INSTANCE.settings.secondaryColor.getColor().getRGB() & 0xFFFFFF) | ((int) (opacity * 255) << 24));
            // Scale back down
            GL11.glScaled(1 / subheaderScale, 1 / subheaderScale, 1 / subheaderScale);
        }

        /*
         * Draw options list background
         */

        drawRect((int) (width * boxMargins), topOfBox, (int) (width * (1 - boxMargins)), height, 0x000000 | ((int) (opacity * 0.5 * 255) << 24));

        drawScrollbar((int) ((width * (1 - boxMargins)) - scrollbarWidth) - ConfigElement.ELEMENT_MARGINS);

        /*
         * Draw buttons
         * super.drawScreen override
         */
        updateOpacity();
        for (QuickplayGuiComponent component : componentList) {
            if(!component.displayString.equals(openFolderText)) {
                double scrollOpacity = ((component.y - scrollPixel) > topOfBox ? 1 : (component.y - scrollPixel) + ConfigElement.ELEMENT_HEIGHT < topOfBox ? 0 : (fadeDistance - ((double) topOfBox - (double) (component.y - scrollPixel))) / (double) fadeDistance);
                if((component.y - scrollPixel) + fadeDistance > topOfBox) component.draw(this, mouseX, mouseY, opacity * (float) scrollOpacity);
            } else
                component.draw(this, mouseX, mouseY, opacity);
        }

        /*
         * Draw description text label
         */
        if(mouseStandStillTicks >= hoverDelayTicks) {
            for (QuickplayGuiComponent component : componentList) {
                if(component.origin instanceof ConfigElement && component.y - scrollPixel > scrollFrameTop - fadeDistance) {
                    int y = component.y;
                    if(component.scrollable) y -= scrollPixel;

                    if((component.x < mouseX && component.x + component.width > mouseX) && (y < mouseY && y + component.height > mouseY)) {
                        final ConfigElement element = (ConfigElement) component.origin;
                        if(element != null && element.optionInfo != null && I18n.format(element.optionInfo.category()).length() > 0) {
                            final List<String> text = new ArrayList<>();
                            text.add(I18n.format(element.optionInfo.helpText()));
                            drawHoveringText(text, mouseX, mouseY, mc.fontRenderer);
                        }
                        break;
                    }
                }
            }
        }

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    /**
     * Get the Y location of the element with the given ID
     * Elements are given an ID, that ID being a zero-indexed position within the list of elements.
     * Therefore we can calculate the Y value for the element if we have it's ID and each element's height
     * @param id ID of the element to check
     * @return The value along the Y axis that this element rests on
     */
    public int getElementY(int id) {
        return (topOfBox + ConfigElement.ELEMENT_MARGINS + ((ConfigElement.ELEMENT_HEIGHT + ConfigElement.ELEMENT_MARGINS) * (id)));
    }

    @Override
    public void componentClicked(QuickplayGuiComponent component) {
        // Only do something if the component is visible
        if(component.y - scrollPixel > scrollFrameTop - fadeDistance || !component.scrollable) {
            super.componentClicked(component);

            if(component.origin instanceof ConfigElement) {
                final ConfigElement element = (ConfigElement) component.origin;
                if(element != null) {
                    if(element.element instanceof Boolean) {
                        element.element = !(boolean) element.element;
                        component.displayString = I18n.format(element.optionInfo.name()) + ": " + I18n.format((boolean) element.element ? "quickplay.config.gui.true" : "quickplay.config.gui.false");

                        // Send analytical data to Google
                        if(Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null && Quickplay.INSTANCE.usageStats.sendUsageStats && Quickplay.INSTANCE.ga != null) {
                            Quickplay.INSTANCE.threadPool.submit(() -> {
                                try {
                                    Quickplay.INSTANCE.ga.createEvent("Config", "Boolean Changed")
                                            .setEventLabel(element.configFieldName + " : " + element.element)
                                            .send();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    } else if(element.element instanceof Runnable) {
                        mc.displayGuiScreen(null);
                        ((Runnable) element.element).run();

                        // Send analytical data to Google
                        if(Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null && Quickplay.INSTANCE.usageStats.sendUsageStats && Quickplay.INSTANCE.ga != null) {
                            Quickplay.INSTANCE.threadPool.submit(() -> {
                                try {
                                    Quickplay.INSTANCE.ga.createEvent("Config", "Runnable Clicked")
                                            .setEventLabel(element.configFieldName)
                                            .send();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    } else if(element.element instanceof QuickplayColor) {
                        mc.displayGuiScreen(new QuickplayGuiEditColor((QuickplayColor) element.element, I18n.format(element.optionInfo.name()), config, this));
                    } else if(element.element.getClass().isEnum()) {
                        // Find out what the next enum in the list is
                        final List list = Arrays.asList(element.element.getClass().getEnumConstants());
                        final int index = list.indexOf(element.element);
                        final int nextIndex = list.size() > index + 1 ? index + 1 : 0;
                        element.element = list.get(nextIndex);

                        component.displayString = I18n.format(element.optionInfo.name()) + ": " + I18n.format(String.valueOf(element.element));
                    } else if(element.element instanceof Double) {
                        // Send analytical data to Google
                        if(Quickplay.INSTANCE.usageStats != null && Quickplay.INSTANCE.usageStats.statsToken != null && Quickplay.INSTANCE.usageStats.sendUsageStats && Quickplay.INSTANCE.ga != null) {
                            Quickplay.INSTANCE.threadPool.submit(() -> {
                                try {
                                    Quickplay.INSTANCE.ga.createEvent("Config", "Slider Changed")
                                            .setEventLabel(element.configFieldName + " : " + element.element)
                                            .send();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }

                    save(element);
                }
            } else if(component.displayString.equals(openFolderText)) {
                try {
                    Desktop.getDesktop().open(new File(AssetFactory.configDirectory));
                } catch (IOException e) {
                    e.printStackTrace();
                    Quickplay.INSTANCE.sendExceptionRequest(e);
                }
            }
        }
    }

    /**
     * Save the given configuration element to the config
     * @param element Element to write & save
     */
    public void save(ConfigElement element) {
        // Try to apply the changed value to the config & then save the config
        try {
            // If field is final, don't try to overwrite
            if(!Modifier.isFinal(config.getClass().getField(element.configFieldName).getModifiers())) {
                config.getClass().getField(element.configFieldName).set(config, element.element);
                config.save();
            }
        } catch (IOException | IllegalAccessException | NoSuchFieldException e) {
            System.out.println("Failed to save option " + element.configFieldName + ".");
            Quickplay.INSTANCE.messageBuffer.push(new Message(new TextComponentTranslation("quickplay.config.saveerror").setStyle(new Style().setColor(TextFormatting.RED))));
            e.printStackTrace();
            Quickplay.INSTANCE.sendExceptionRequest(e);
        }
    }

    @Override
    public void mouseScrolled(int distance) {
        super.mouseScrolled(distance);
        // On scroll let's clear the arrays containing mouse positions used for hover text
        lastTwoMouseX = new int[]{-1, -1};
        lastTwoMouseY = new int[]{-1, -1};

    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        // Increase the number of ticks the mouse has been standing still if necessary
        if(event.phase == TickEvent.Phase.START)
            if(Math.abs(lastTwoMouseX[0] - lastTwoMouseX[1]) < mouseStandStillMargin && Math.abs(lastTwoMouseY[0] - lastTwoMouseY[1]) < mouseStandStillMargin)
                mouseStandStillTicks++;
    }

    // ------

    /**
     * GUI responder used for detecting movement on GUI sliders
     */
    public class ConfigGuiResponder implements GuiPageButtonList.GuiResponder {

        /**
         * Fired every tick for a boolean-based GUI element change
         * @param p_175321_1_ ID of the element
         * @param p_175321_2_ Value
         */
        @Override
        public void setEntryValue(int p_175321_1_, boolean p_175321_2_) {

        }

        /**
         * Fired every tick for a float-based GUI element change
         * @param id ID of the element
         * @param value Value
         */
        @Override
        public void setEntryValue(int id, float value) {
            ConfigElement element = (ConfigElement) componentList.get(id).origin;
            element.element = ((Number) value).doubleValue();
            save(element);
        }

        /**
         * Fired every tick for a text-based GUI element change
         * @param p_175319_1_ ID of the element
         * @param p_175319_2_ Value
         */
        @Override
        @ParametersAreNonnullByDefault
        public void setEntryValue(int p_175319_1_, String p_175319_2_) {

        }
    }

    /**
     * Format helper for figuring out the display text for GUI sliders
     */
    public class SliderFormatHelper implements QuickplayGuiSlider.FormatHelper {
        @Override
        public String getText(int id, String name, float value) {
            String pattern;
            if(componentList.size() >= id + 1 && componentList.get(id).origin != null)
                pattern = ((ConfigElement) componentList.get(id).origin).optionInfo.decimalFormat();
            else
                pattern = "0.00";

            final DecimalFormat format = new DecimalFormat(pattern);
            return name + ": " + format.format(value);
        }
    }
}
