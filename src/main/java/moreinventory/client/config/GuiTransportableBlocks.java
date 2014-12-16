package moreinventory.client.config;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import cpw.mods.fml.client.config.GuiButtonExt;
import cpw.mods.fml.client.config.GuiConfigEntries.ArrayEntry;
import moreinventory.client.gui.GuiListSlot;
import moreinventory.core.MoreInventoryMod;
import moreinventory.util.ArrayListExtended;
import moreinventory.util.BlockMeta;
import moreinventory.util.MIMUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveAction;

public class GuiTransportableBlocks extends GuiScreen
{
	private final GuiScreen parent;
	protected ArrayEntry configElement;

	protected ChestList chestList;
	protected GuiButton doneButton, editButton, cancelButton, addButton, removeButton, clearButton;
	protected GuiTextField filterTextField;

	protected boolean editMode;
	protected GuiTextField blockField, blockMetaField, iconField;

	private int maxLabelWidth;

	private final List<String> editLabelList = Lists.newArrayList();
	private final List<GuiTextField> editFieldList = Lists.newArrayList();

	public GuiTransportableBlocks(GuiScreen parent)
	{
		this.parent = parent;
	}

	public GuiTransportableBlocks(GuiScreen parent, ArrayEntry entry)
	{
		this(parent);
		this.configElement = entry;
	}

	@Override
	public void initGui()
	{
		Keyboard.enableRepeatEvents(true);

		if (chestList == null)
		{
			chestList = new ChestList();
		}

		chestList.func_148122_a(width, height, 32, height - (editMode ? 74 : 28));

		if (doneButton == null)
		{
			doneButton = new GuiButtonExt(0, 0, 0, 65, 20, I18n.format("gui.done"));
		}

		doneButton.xPosition = width / 2 + 135;
		doneButton.yPosition = height - doneButton.height - 4;

		if (editButton == null)
		{
			editButton = new GuiButtonExt(1, 0, 0, doneButton.width, doneButton.height, I18n.format("gui.edit"));
			editButton.enabled = false;
		}

		editButton.xPosition = doneButton.xPosition - doneButton.width - 3;
		editButton.yPosition = doneButton.yPosition;
		editButton.enabled = chestList.selected != null;
		editButton.visible = !editMode;

		if (cancelButton == null)
		{
			cancelButton = new GuiButtonExt(2, 0, 0, editButton.width, editButton.height, I18n.format("gui.cancel"));
		}

		cancelButton.xPosition = editButton.xPosition;
		cancelButton.yPosition = editButton.yPosition;
		cancelButton.visible = editMode;

		if (removeButton == null)
		{
			removeButton = new GuiButtonExt(4, 0, 0, doneButton.width, doneButton.height, I18n.format("gui.remove"));
		}

		removeButton.xPosition = editButton.xPosition - editButton.width - 3;
		removeButton.yPosition = doneButton.yPosition;
		removeButton.visible =  !editMode;

		if (addButton == null)
		{
			addButton = new GuiButtonExt(3, 0, 0, doneButton.width, doneButton.height, I18n.format("gui.add"));
		}

		addButton.xPosition = removeButton.xPosition - removeButton.width - 3;
		addButton.yPosition = doneButton.yPosition;
		addButton.visible = !editMode;

		if (clearButton == null)
		{
			clearButton = new GuiButtonExt(5, 0, 0, removeButton.width, removeButton.height, I18n.format("gui.clear"));
		}

		clearButton.xPosition = removeButton.xPosition;
		clearButton.yPosition = removeButton.yPosition;
		clearButton.visible = false;

		buttonList.clear();
		buttonList.add(doneButton);

		if (editMode)
		{
			buttonList.add(cancelButton);
		}
		else
		{
			buttonList.add(editButton);
			buttonList.add(addButton);
			buttonList.add(removeButton);
			buttonList.add(clearButton);
		}

		if (filterTextField == null)
		{
			filterTextField = new GuiTextField(fontRendererObj, 0, 0, 122, 16);
			filterTextField.setMaxStringLength(500);
		}

		filterTextField.xPosition = width / 2 - 200;
		filterTextField.yPosition = height - filterTextField.height - 6;

		editLabelList.clear();
		editLabelList.add(I18n.format("moreinv.config.edit.block"));
		editLabelList.add("");
		editLabelList.add(I18n.format("moreinv.config.edit.iconIndex"));

		for (String key : editLabelList)
		{
			maxLabelWidth = Math.max(maxLabelWidth, fontRendererObj.getStringWidth(key));
		}

		if (blockField == null)
		{
			blockField = new GuiTextField(fontRendererObj, 0, 0, 0, 15);
			blockField.setMaxStringLength(100);
		}

		int i = maxLabelWidth + 8 + width / 2;
		blockField.xPosition = width / 2 - i / 2 + maxLabelWidth + 10;
		blockField.yPosition = chestList.bottom + 5;
		int fieldWidth = width / 2 + i / 2 - 45 - blockField.xPosition + 40;
		blockField.width = fieldWidth / 4 + fieldWidth / 2 - 1;

		if (blockMetaField == null)
		{
			blockMetaField = new GuiTextField(fontRendererObj, 0, 0, 0, blockField.height);
			blockMetaField.setMaxStringLength(2);
		}

		blockMetaField.xPosition = blockField.xPosition + blockField.width + 3;
		blockMetaField.yPosition = blockField.yPosition;
		blockMetaField.width = fieldWidth / 4 - 1;

		if (iconField == null)
		{
			iconField = new GuiTextField(fontRendererObj, 0, 0, 0, blockField.height);
			iconField.setMaxStringLength(2);
		}

		iconField.xPosition = blockField.xPosition;
		iconField.yPosition = blockField.yPosition + blockField.height + 5;
		iconField.width = fieldWidth;

		editFieldList.clear();

		if (editMode)
		{
			editFieldList.add(blockField);
			editFieldList.add(blockMetaField);
			editFieldList.add(iconField);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if (!button.enabled)
		{
			return;
		}

		switch (button.id)
		{
			case 0:
				if (editMode)
				{
					if (chestList.selected != null)
					{
						if (!Strings.isNullOrEmpty(blockField.getText()))
						{
							chestList.selected.blockMeta = new BlockMeta(blockField.getText(), NumberUtils.toInt(blockMetaField.getText()));
						}

						if (!Strings.isNullOrEmpty(iconField.getText()))
						{
							chestList.selected.iconIndex = NumberUtils.toInt(iconField.getText(), 19);
						}
					}

					actionPerformed(cancelButton);

					chestList.scrollToTop();
				}
				else
				{
					if (configElement != null)
					{
						MIMUtils.getPool().execute(new RecursiveAction()
						{
							@Override
							protected void compute()
							{
								List<String> values = Lists.newArrayList();

								for (ChestEntry entry : chestList.chests)
								{
									values.add(entry.toString());
								}

								configElement.setListFromChildScreen(values.toArray());
							}
						});
					}

					actionPerformed(cancelButton);

					chestList.scrollToTop();
				}

				break;
			case 1:
				if (editMode)
				{
					actionPerformed(cancelButton);
				}
				else
				{
					editMode = true;
					initGui();

					chestList.scrollToTop();

					if (chestList.selected != null)
					{
						blockField.setText(MIMUtils.getUniqueName(chestList.selected.blockMeta.block));
						blockMetaField.setText(Integer.toString(chestList.selected.blockMeta.meta));
						iconField.setText(Integer.toString(chestList.selected.iconIndex));
					}
				}

				break;
			case 2:
				if (editMode)
				{
					editMode = false;
					initGui();
				}
				else
				{
					mc.displayGuiScreen(parent);

					if (parent == null)
					{
						mc.setIngameFocus();
					}
				}

				break;
			case 3:
				mc.displayGuiScreen(new GuiSelectTransportableBlock(this));
				break;
			case 4:
				MIMUtils.getPool().execute(new RecursiveAction()
				{
					@Override
					protected void compute()
					{
						chestList.chests.remove(chestList.selected);
						chestList.contents.remove(chestList.selected);
						chestList.selected = null;
					}
				});

				break;
			case 5:
				chestList.chests.clear();
				chestList.contents.clear();
				chestList.selected = null;
				break;
		}
	}

	@Override
	public void updateScreen()
	{
		if (editMode)
		{
			for (GuiTextField textField : editFieldList)
			{
				textField.updateCursorCounter();
			}
		}
		else
		{
			editButton.enabled = chestList.selected != null;
			removeButton.enabled = editButton.enabled;

			filterTextField.updateCursorCounter();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float ticks)
	{
		chestList.drawScreen(mouseX, mouseY, ticks);

		drawCenteredString(fontRendererObj, I18n.format(MoreInventoryMod.CONFIG_LANG + "general.transportableBlocks"), width / 2, 15, 0xFFFFFF);

		super.drawScreen(mouseX, mouseY, ticks);

		if (editMode)
		{
			GuiTextField textField;

			for (int i = 0; i < editFieldList.size(); ++i)
			{
				textField = editFieldList.get(i);
				textField.drawTextBox();
				drawString(fontRendererObj, editLabelList.get(i), textField.xPosition - maxLabelWidth - 10, textField.yPosition + 3, 0xBBBBBB);
			}
		}
		else
		{
			filterTextField.drawTextBox();
		}
	}

	@Override
	public void handleMouseInput()
	{
		super.handleMouseInput();

		if (blockMetaField.isFocused())
		{
			int i = Mouse.getDWheel();

			if (i < 0)
			{
				blockMetaField.setText(Integer.toString(Math.max(NumberUtils.toInt(blockMetaField.getText()) - 1, -1)));
			}
			else if (i > 0)
			{
				blockMetaField.setText(Integer.toString(Math.min(NumberUtils.toInt(blockMetaField.getText()) + 1, 15)));
			}
		}
		else if (iconField.isFocused())
		{
			int i = Mouse.getDWheel();

			if (i < 0)
			{
				iconField.setText(Integer.toString(Math.max(NumberUtils.toInt(iconField.getText()) - 1, 0)));
			}
			else if (i > 0)
			{
				iconField.setText(Integer.toString(Math.min(NumberUtils.toInt(iconField.getText()) + 1, 30)));
			}
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int code)
	{
		super.mouseClicked(x, y, code);

		if (editMode)
		{
			for (GuiTextField textField : editFieldList)
			{
				textField.mouseClicked(x, y, code);
			}

			if (!isShiftKeyDown())
			{
				if (blockField.isFocused())
				{
					blockField.setFocused(false);

					mc.displayGuiScreen(new GuiSelectTransportableBlock(this, blockField, blockMetaField));
				}
				else if (iconField.isFocused())
				{
					iconField.setFocused(false);

					mc.displayGuiScreen(new GuiSelectTransportableIcon(this, iconField));
				}
			}
		}
		else
		{
			filterTextField.mouseClicked(x, y, code);
		}
	}

	@Override
	public void handleKeyboardInput()
	{
		super.handleKeyboardInput();

		if (Keyboard.getEventKey() == Keyboard.KEY_LSHIFT || Keyboard.getEventKey() == Keyboard.KEY_RSHIFT)
		{
			clearButton.visible = !editMode && Keyboard.getEventKeyState();
		}
	}

	@Override
	protected void keyTyped(char c, int code)
	{
		if (editMode)
		{
			for (GuiTextField textField : editFieldList)
			{
				if (code == Keyboard.KEY_ESCAPE)
				{
					textField.setFocused(false);
				}
				else if (textField.isFocused())
				{
					if (textField == iconField)
					{
						if (!CharUtils.isAsciiControl(c) && !CharUtils.isAsciiNumeric(c))
						{
							continue;
						}
					}

					textField.textboxKeyTyped(c, code);
				}
			}
		}
		else
		{
			if (filterTextField.isFocused())
			{
				if (code == Keyboard.KEY_ESCAPE)
				{
					filterTextField.setFocused(false);
				}

				String prev = filterTextField.getText();

				filterTextField.textboxKeyTyped(c, code);

				String text = filterTextField.getText();
				boolean changed = !text.equals(prev);

				if (Strings.isNullOrEmpty(text) && changed)
				{
					chestList.setFilter(null);
				}
				else if (changed || code == Keyboard.KEY_RETURN)
				{
					chestList.setFilter(text);
				}
			}
			else
			{
				if (code == Keyboard.KEY_ESCAPE)
				{
					mc.displayGuiScreen(parent);

					if (parent == null)
					{
						mc.setIngameFocus();
					}
				}
				else if (code == Keyboard.KEY_BACK)
				{
					chestList.selected = null;
				}
				else if (code == Keyboard.KEY_TAB)
				{
					if (++chestList.nameType > 1)
					{
						chestList.nameType = 0;
					}
				}
				else if (code == Keyboard.KEY_UP)
				{
					chestList.scrollUp();
				}
				else if (code == Keyboard.KEY_DOWN)
				{
					chestList.scrollDown();
				}
				else if (code == Keyboard.KEY_HOME)
				{
					chestList.scrollToTop();
				}
				else if (code == Keyboard.KEY_END)
				{
					chestList.scrollToEnd();
				}
				else if (code == Keyboard.KEY_PRIOR)
				{
					chestList.scrollToPrev();
				}
				else if (code == Keyboard.KEY_NEXT)
				{
					chestList.scrollToNext();
				}
				else if (code == Keyboard.KEY_F || code == mc.gameSettings.keyBindChat.getKeyCode())
				{
					filterTextField.setFocused(true);
				}
			}
		}
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();

		Keyboard.enableRepeatEvents(false);
	}

	class ChestEntry
	{
		BlockMeta blockMeta;
		int iconIndex;

		public ChestEntry(BlockMeta blockMeta, int icon)
		{
			this.blockMeta = blockMeta;
			this.iconIndex = icon;
		}

		@Override
		public int hashCode()
		{
			return Objects.hashCode(blockMeta, iconIndex);
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof ChestEntry)
			{
				ChestEntry entry = (ChestEntry)obj;

				return blockMeta == entry.blockMeta && iconIndex == entry.iconIndex;
			}

			return false;
		}

		@Override
		public String toString()
		{
			return blockMeta.toString() + "@" + iconIndex;
		}
	}

	protected class ChestList extends GuiListSlot<ChestEntry> implements Comparator<ChestEntry>
	{
		protected final ArrayListExtended<ChestEntry>
		chests = new ArrayListExtended<>(),
		contents = new ArrayListExtended<>();
		private final Map<String, List<ChestEntry>> filterCache = Maps.newHashMap();

		protected int nameType;
		protected ChestEntry selected;

		public ChestList()
		{
			super(GuiTransportableBlocks.this.mc, 0, 0, 0, 0, 22);
			this.initEntries();
		}

		private void initEntries()
		{
			MIMUtils.getPool().execute(new RecursiveAction()
			{
				@Override
				protected void compute()
				{
					chests.clear();
					contents.clear();
					filterCache.clear();
					selected = null;

					if (configElement != null)
					{
						for (Object obj : configElement.getCurrentValues())
						{
							String str = String.valueOf(obj);

							if (Strings.isNullOrEmpty(str) || str.equals("null") || !str.contains("@"))
							{
								continue;
							}

							String[] args = str.split("@");
							BlockMeta blockMeta = new BlockMeta(args[0], NumberUtils.toInt(args[1]));
							int icon = args.length > 2 ? NumberUtils.toInt(args[2], 19) : 19;

							if (blockMeta.block != Blocks.air)
							{
								chests.addIfAbsent(new ChestEntry(blockMeta, icon));
							}
						}
					}

					contents.addAll(chests);
				}
			});
		}

		@Override
		protected ArrayListExtended<ChestEntry> getContents()
		{
			return contents;
		}

		@Override
		protected void drawBackground()
		{
			drawDefaultBackground();
		}

		@Override
		protected void drawSlot(int index, int par2, int par3, int par4, Tessellator tessellator, int mouseX, int mouseY)
		{
			ChestEntry entry = getContents().get(index, null);

			if (entry == null)
			{
				return;
			}

			ItemStack itemstack = new ItemStack(entry.blockMeta.block, 1, entry.blockMeta.meta);
			String name = null;

			try
			{
				if (itemstack.getItem() == null)
				{
					switch (nameType)
					{
						case 1:
							name = MIMUtils.getUniqueName(entry.blockMeta.block);
							break;
						case 2:
							name = entry.blockMeta.block.getUnlocalizedName();
							name = name.substring(name.indexOf(".") + 1);
							break;
						default:
							name = entry.blockMeta.block.getLocalizedName();
							break;
					}
				}
				else
				{
					switch (nameType)
					{
						case 1:
							name = MIMUtils.getUniqueName(entry.blockMeta.block) + ", " + itemstack.getItemDamage();
							break;
						case 2:
							name = itemstack.getUnlocalizedName();
							name = name.substring(name.indexOf(".") + 1);
							break;
						default:
							name = itemstack.getDisplayName();
							break;
					}
				}
			}
			catch (Throwable ignored) {}

			itemstack = new ItemStack(MoreInventoryMod.transporter);

			if (entry.blockMeta.block == Blocks.chest)
			{
				itemstack.setItemDamage(1);
			}
			else if (entry.blockMeta.block == Blocks.trapped_chest)
			{
				itemstack.setItemDamage(2);
			}
			else if (entry.blockMeta.block == Blocks.furnace)
			{
				itemstack.setItemDamage(3);
			}
			else if (entry.blockMeta.block == Blocks.lit_furnace)
			{
				itemstack.setItemDamage(4);
			}
			else if (entry.blockMeta.block == MoreInventoryMod.storageBox)
			{
				if (entry.blockMeta.meta == -1)
				{
					itemstack.setItemDamage(5);
				}
				else
				{
					itemstack.setItemDamage(entry.blockMeta.meta + 5);
				}
			}
			else
			{
				itemstack.setItemDamage(entry.iconIndex + 21);
			}

			try
			{
				GL11.glPushMatrix();
				GL11.glTranslatef(0.0F, 0.0F, 32.0F);
				GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
				GL11.glEnable(GL11.GL_LIGHTING);
				RenderHelper.enableGUIStandardItemLighting();
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
				itemRender.zLevel += 100.0F;
				itemRender.renderItemIntoGUI(fontRendererObj, mc.getTextureManager(), itemstack, width / 2 - 100, par3 + 1);
				itemRender.zLevel -= 100.0F;
				GL11.glPopMatrix();
				GL11.glDisable(GL12.GL_RESCALE_NORMAL);
				GL11.glDisable(GL11.GL_LIGHTING);
			}
			catch (Throwable ignored) {}

			if (!Strings.isNullOrEmpty(name))
			{
				drawCenteredString(fontRendererObj, name, width / 2, par3 + 1, 0xFFFFFF);
			}
		}

		@Override
		protected void elementClicked(int index, boolean flag, int mouseX, int mouseY)
		{
			selected = getContents().get(index, null);
		}

		@Override
		protected boolean isSelected(int index)
		{
			ChestEntry entry = getContents().get(index, null);

			return entry != null && selected == entry;
		}

		protected void setFilter(final String filter)
		{
			MIMUtils.getPool().execute(new RecursiveAction()
			{
				@Override
				protected void compute()
				{
					List<ChestEntry> result;

					if (Strings.isNullOrEmpty(filter))
					{
						result = chests;
					}
					else if (filter.equals("selected"))
					{
						result = Lists.newArrayList(selected);
					}
					else
					{
						if (!filterCache.containsKey(filter))
						{
							filterCache.put(filter, Lists.newArrayList(Collections2.filter(chests, new Predicate<ChestEntry>()
							{
								@Override
								public boolean apply(ChestEntry entry)
								{
									return MIMUtils.blockMetaFilter(entry.blockMeta, filter);
								}
							})));
						}

						result = filterCache.get(filter);
					}

					if (!getContents().equals(result))
					{
						getContents().clear();
						getContents().addAll(result);
					}
				}
			});
		}

		@Override
		public int compare(ChestEntry o1, ChestEntry o2)
		{
			int i = MIMUtils.compareWithNull(o1, o2);

			if (i == 0 && o1 != null && o2 != null)
			{
				i = MIMUtils.blockMetaComparator.compare(o1.blockMeta, o2.blockMeta);

				if (i == 0)
				{
					i = Integer.compare(o1.iconIndex, o2.iconIndex);
				}
			}

			return i;
		}
	}
}