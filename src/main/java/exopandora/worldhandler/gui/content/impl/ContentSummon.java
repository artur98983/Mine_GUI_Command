package exopandora.worldhandler.gui.content.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import exopandora.worldhandler.builder.ICommandBuilder;
import exopandora.worldhandler.builder.component.impl.ComponentAttribute;
import exopandora.worldhandler.builder.impl.BuilderSummon;
import exopandora.worldhandler.config.Config;
import exopandora.worldhandler.gui.category.Categories;
import exopandora.worldhandler.gui.category.Category;
import exopandora.worldhandler.gui.container.Container;
import exopandora.worldhandler.gui.content.Content;
import exopandora.worldhandler.gui.content.Contents;
import exopandora.worldhandler.gui.menu.impl.ILogicColorMenu;
import exopandora.worldhandler.gui.menu.impl.ILogicPageList;
import exopandora.worldhandler.gui.menu.impl.MenuColorField;
import exopandora.worldhandler.gui.menu.impl.MenuPageList;
import exopandora.worldhandler.gui.widget.button.EnumIcon;
import exopandora.worldhandler.gui.widget.button.GuiButtonBase;
import exopandora.worldhandler.gui.widget.button.GuiButtonIcon;
import exopandora.worldhandler.gui.widget.button.GuiButtonItem;
import exopandora.worldhandler.gui.widget.button.GuiSlider;
import exopandora.worldhandler.gui.widget.button.GuiTextFieldTooltip;
import exopandora.worldhandler.gui.widget.button.LogicSliderAttribute;
import exopandora.worldhandler.gui.widget.button.LogicSliderSimple;
import exopandora.worldhandler.util.ActionHandler;
import exopandora.worldhandler.util.ActionHelper;
import exopandora.worldhandler.util.CommandHelper;
import exopandora.worldhandler.util.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

public class ContentSummon extends Content
{
	private static final ResourceLocation BEACON_LOCATION = new ResourceLocation("textures/gui/container/beacon.png");
	
	private GuiTextFieldTooltip mobField;
	private GuiTextFieldTooltip nbtField;
	
	private int potionPage = 0;
	private boolean editColor;
	
	private Page page = Page.START;
	private String mob;
	private String nbt;
	
	private final BuilderSummon builderSummon = new BuilderSummon();
	
	private final ResourceLocation[] helmets =
	{
			Blocks.AIR.getRegistryName(),
			Items.LEATHER_HELMET.getRegistryName(),
			Items.IRON_HELMET.getRegistryName(),
			Items.CHAINMAIL_HELMET.getRegistryName(),
			Items.GOLDEN_HELMET.getRegistryName(),
			Items.DIAMOND_HELMET.getRegistryName(),
			Items.NETHERITE_HELMET.getRegistryName()
	};
	private final ResourceLocation[] chestplates =
	{
			Blocks.AIR.getRegistryName(),
			Items.LEATHER_CHESTPLATE.getRegistryName(),
			Items.IRON_CHESTPLATE.getRegistryName(),
			Items.CHAINMAIL_CHESTPLATE.getRegistryName(),
			Items.GOLDEN_CHESTPLATE.getRegistryName(),
			Items.DIAMOND_CHESTPLATE.getRegistryName(),
			Items.NETHERITE_CHESTPLATE.getRegistryName()
	};
	private final ResourceLocation[] leggings =
	{
			Blocks.AIR.getRegistryName(),
			Items.LEATHER_LEGGINGS.getRegistryName(),
			Items.IRON_LEGGINGS.getRegistryName(),
			Items.CHAINMAIL_LEGGINGS.getRegistryName(),
			Items.GOLDEN_LEGGINGS.getRegistryName(),
			Items.DIAMOND_LEGGINGS.getRegistryName(),
			Items.NETHERITE_LEGGINGS.getRegistryName()
	};
	private final ResourceLocation[] boots =
	{
			Blocks.AIR.getRegistryName(),
			Items.LEATHER_BOOTS.getRegistryName(),
			Items.IRON_BOOTS.getRegistryName(),
			Items.CHAINMAIL_BOOTS.getRegistryName(),
			Items.GOLDEN_BOOTS.getRegistryName(),
			Items.DIAMOND_BOOTS.getRegistryName(),
			Items.NETHERITE_BOOTS.getRegistryName()
	};
	private final ResourceLocation[] swords =
	{
			Blocks.AIR.getRegistryName(),
			Items.WOODEN_SWORD.getRegistryName(),
			Items.STONE_SWORD.getRegistryName(),
			Items.IRON_SWORD.getRegistryName(),
			Items.GOLDEN_SWORD.getRegistryName(),
			Items.DIAMOND_SWORD.getRegistryName(),
			Items.NETHERITE_SWORD.getRegistryName()
	};
	private final ResourceLocation[][] armor = {this.helmets, this.chestplates, this.leggings, this.boots};
	private final ResourceLocation[][] hands = {this.swords, this.swords};
	
	@Override
	public ICommandBuilder getCommandBuilder()
	{
		return this.builderSummon;
	}
	
	@Override
	public void init(Container container)
	{
		for(Attribute attribute : this.builderSummon.getAttributes())
		{
			double ammount = this.builderSummon.getAttributeAmmount(attribute);
			
			if(ammount > Config.getSliders().getMaxSummonAttributes())
			{
				this.builderSummon.setAttribute(attribute, Config.getSliders().getMaxSummonAttributes());
			}
		}
		
		for(MobEffect potion : this.builderSummon.getMobEffects())
		{
			byte amplifier = this.builderSummon.getAmplifier(potion);
			
			if(amplifier > Config.getSliders().getMaxSummonPotionAmplifier())
			{
				this.builderSummon.setAmplifier(potion, (byte) Config.getSliders().getMaxSummonPotionAmplifier());
			}
			
			int minutes = this.builderSummon.getMinutes(potion);
			
			if(minutes > Config.getSliders().getMaxSummonPotionMinutes())
			{
				this.builderSummon.setMinutes(potion, (int) Config.getSliders().getMaxSummonPotionMinutes());
			}
		}
	}
	
	@Override
	public void initGui(Container container, int x, int y)
	{
		this.mobField = new GuiTextFieldTooltip(x + 118, y, 114, 20, new TranslatableComponent("gui.worldhandler.entities.summon.start.mob_id"));
		this.mobField.setFilter(Predicates.notNull());
		this.mobField.setValue(this.mob);
		this.mobField.setResponder(text ->
		{
			this.mob = text;
			this.builderSummon.setName(this.mob);
			container.initButtons();
		});
		
		this.nbtField = new GuiTextFieldTooltip(x + 118, y + 48, 114, 20, new TranslatableComponent("gui.worldhandler.entities.summon.start.custom_nbt"));
		this.nbtField.setFilter(Predicates.notNull());
		this.nbtField.setValue(this.nbt);
		this.nbtField.setResponder(text ->
		{
			this.nbt = text;
			this.builderSummon.setEntityNBT(this.nbt);
			container.initButtons();
		});
		
		if(Page.START.equals(this.page))
		{
			MenuColorField customName = new MenuColorField(x, y, "gui.worldhandler.entities.summon.start.custom_name", this.builderSummon.getCustomName(), new ILogicColorMenu()
			{
				@Override
				public boolean doDrawButtons()
				{
					return ContentSummon.this.editColor;
				}
				
				@Override
				public boolean doDrawTextField()
				{
					return ContentSummon.this.editColor;
				}
				
				@Override
				public String getId()
				{
					return "custom_name";
				}
			});
			
			container.add(customName);
		}
		else if(Page.ATTRIBUTES.equals(this.page))
		{
			MenuPageList<Attribute> attributes = new MenuPageList<Attribute>(x + 118, y, ComponentAttribute.ATTRIBUTES, 114, 20, 3, container, new ILogicPageList<Attribute>()
			{
				@Override
				public MutableComponent translate(Attribute item)
				{
					return new TranslatableComponent(item.getDescriptionId());
				}
				
				@Override
				public MutableComponent toTooltip(Attribute item)
				{
					return new TextComponent(item.getRegistryName().toString());
				}
				
				@Override
				public void onClick(Attribute item)
				{
					
				}
				
				@Override
				public GuiButtonBase onRegister(int x, int y, int width, int height, MutableComponent text, Attribute item, ActionHandler actionHandler)
				{
					return new GuiSlider(x, y, width, height, -Config.getSliders().getMaxSummonAttributes(), Config.getSliders().getMaxSummonAttributes(), 0, container, new LogicSliderAttribute(item, text, value ->
					{
						ContentSummon.this.builderSummon.setAttribute(item, value);
					}));
				}
				
				@Override
				public boolean doDisable()
				{
					return false;
				}
				
				@Override
				public String getId()
				{
					return "attributes";
				}
			});
			
			container.add(attributes);
		}
	}
	
	@Override
	public void initButtons(Container container, int x, int y)
	{
		GuiButtonBase button1;
		GuiButtonBase button2;
		GuiButtonBase button3;
		GuiButtonBase button4;
		GuiButtonBase button5;
		GuiButtonBase button6;
		GuiButtonBase button7;
		
		container.add(new GuiButtonBase(x, y + 96, 114, 20, "gui.worldhandler.generic.back", () -> ActionHelper.back(this)));
		container.add(new GuiButtonBase(x + 118, y + 96, 114, 20, "gui.worldhandler.generic.backToGame", ActionHelper::backToGame));
		
		container.add(button4 = new GuiButtonBase(x, y, 114, 20, "gui.worldhandler.entities.summon.start", () -> this.changePage(container, Page.START)));
		container.add(button5 = new GuiButtonBase(x, y + 24, 114, 20, "gui.worldhandler.entities.summon.potion_effects", () -> this.changePage(container, Page.POTIONS)));
		container.add(button6 = new GuiButtonBase(x, y + 48, 114, 20, "gui.worldhandler.entities.summon.attributes", () -> this.changePage(container, Page.ATTRIBUTES)));
		container.add(button7 = new GuiButtonBase(x, y + 72, 114, 20, "gui.worldhandler.entities.summon.equipment", () -> this.changePage(container, Page.EQUIPMENT)));
		
		if(Page.START.equals(this.page))
		{
			button4.active = false;
			
			if(this.editColor)
			{
				container.add(new GuiButtonBase(x + 118, y + 72, 114, 20, "gui.worldhandler.generic.done", () -> this.toggleEditColor(container)));
			}
			else
			{
				container.add(this.mobField);
				container.add(new GuiButtonBase(x + 118, y + 24, 114, 20, "gui.worldhandler.entities.summon.start.custom_name", () -> this.toggleEditColor(container)));
				container.add(this.nbtField);
				
				if(!this.builderSummon.needsCommandBlock() && !this.builderSummon.getCustomName().isSpecial())
				{
					container.add(button3 = new GuiButtonBase(x + 118, y + 72, 114, 20, "gui.worldhandler.title.entities.summon", () -> this.send(container.getPlayer())));
				}
				else
				{
					container.add(button3 = new GuiButtonBase(x + 118, y + 72, 114, 20, "gui.worldhandler.actions.place_command_block", () -> this.send(container.getPlayer())));
				}
				
				button3.active = ForgeRegistries.ENTITIES.containsKey(this.builderSummon.getEntity());
			}
		}
		else if(Page.POTIONS.equals(this.page))
		{
			button5.active = false;
			
			container.add(button1 = new GuiButtonBase(x + 118, y + 72, 56, 20, TextUtils.ARROW_LEFT, () ->
			{
				this.potionPage--;
				container.init();
			}));
			container.add(button2 = new GuiButtonBase(x + 118 + 60, y + 72, 55, 20, TextUtils.ARROW_RIGHT, () ->
			{
				this.potionPage++;
				container.init();
			}));
			
			int count = 0;
			
			for(ResourceLocation location : this.sortedPotions())
			{
				MobEffect potion = ForgeRegistries.MOB_EFFECTS.getValue(location);
				
				if(!potion.equals(MobEffects.HARM) && !potion.equals(MobEffects.HEAL))
				{
					if(this.potionPage == 0)
					{
						button1.active = false;
					}
					
					if(this.potionPage == ForgeRegistries.MOB_EFFECTS.getKeys().size() - 3)
					{
						button2.active = false;
					}
					
					if(count == this.potionPage)
					{
						container.add(new GuiSlider(x + 118, y, 114, 20, 0, Config.getSliders().getMaxSummonPotionAmplifier(), 0, container, new LogicSliderSimple("amplifier" + potion.getRegistryName(), new TranslatableComponent(potion.getDescriptionId()), value ->
						{
							this.builderSummon.setAmplifier(potion, value.byteValue());
						})));
						container.add(new GuiSlider(x + 118, y + 24, 114, 20, 0, Config.getSliders().getMaxSummonPotionMinutes(), 0, container, new LogicSliderSimple("duration" + potion.getRegistryName(), new TranslatableComponent("gui.worldhandler.potion.time.minutes"), value ->
						{
							this.builderSummon.setMinutes(potion, value);
						})));
						container.add(new GuiButtonBase(x + 118, y + 48, 114, 20, new TranslatableComponent("gui.worldhandler.potions.effect.particles", this.builderSummon.getShowParticles(potion) ? new TranslatableComponent("gui.worldhandler.generic.on") : new TranslatableComponent("gui.worldhandler.generic.off")), () ->
						{
							this.builderSummon.setShowParticles(potion, !this.builderSummon.getShowParticles(potion));
							container.init();
						}));
						break;
					}
					
					count++;
				}
			}
		}
		else if(Page.ATTRIBUTES.equals(this.page))
		{
			button6.active = false;
		}
		else if(Page.EQUIPMENT.equals(this.page))
		{
			for(int i = 0; i < 4; i++)
	 		{
				final int index = i;
				
				container.add(new GuiButtonBase(x + 118, y + 24 * i, 20, 20, TextUtils.ARROW_LEFT, () ->
				{
					this.builderSummon.setArmorItem(3 - index, this.armor[index][Math.floorMod(ArrayUtils.indexOf(this.armor[index], this.builderSummon.getArmorItem(3 - index)) - 1, this.armor[index].length)]);
					container.init();
				}));
				container.add(button1 = new GuiButtonItem(x + 118 + 24, y + 24 * i, 20, 20, ForgeRegistries.ITEMS.getValue(this.builderSummon.getArmorItem(3 - i)), null));
				container.add(new GuiButtonBase(x + 118 + 47, y + 24 * i, 20, 20, TextUtils.ARROW_RIGHT, () ->
				{
					this.builderSummon.setArmorItem(3 - index, this.armor[index][Math.floorMod(ArrayUtils.indexOf(this.armor[index], this.builderSummon.getArmorItem(3 - index)) + 1, this.armor[index].length)]);
					container.init();
				}));
				
				button1.active = false;
	 		}
			
			for(int i = 0; i < 2; i++)
	 		{
				final int index = i;
				
				container.add(new GuiButtonIcon(x + 118 + 70 + 24 * i, y + 12, 20, 20, EnumIcon.ARROW_UP, null, () ->
				{
					this.builderSummon.setHandItem(index, this.hands[index][Math.floorMod(ArrayUtils.indexOf(this.hands[index], this.builderSummon.getHandItem(index)) - 1, this.hands[index].length)]);
					container.init();
				}));
				container.add(button1 = new GuiButtonItem(x + 118 + 70 + 24 * i, y + 36, 20, 20, ForgeRegistries.ITEMS.getValue(this.builderSummon.getHandItem(i)), null));
				container.add(new GuiButtonIcon(x + 118 + 70 + 24 * i, y + 60, 20, 20, EnumIcon.ARROW_DOWN, null, () ->
				{
					this.builderSummon.setHandItem(index, this.hands[index][Math.floorMod(ArrayUtils.indexOf(this.hands[index], this.builderSummon.getHandItem(index)) + 1, this.hands[index].length)]);
					container.init();
				}));
				
				button1.active = false;
	 		}
			
			button7.active = false;
		}
	}
	
	private void send(String player)
	{
		CommandHelper.sendCommand(player, this.builderSummon, this.builderSummon.getCustomName().isSpecial());
	}
	
	@Override
	public void tick(Container container)
	{
		if(Page.START.equals(this.page))
		{
			if(!this.editColor)
			{
				this.mobField.tick();
				this.nbtField.tick();
			}
		}
	}
	
	@Override
	public void drawScreen(PoseStack matrix, Container container, int x, int y, int mouseX, int mouseY, float partialTicks)
	{
		if(Page.START.equals(this.page))
		{
			if(!this.editColor)
			{
				this.mobField.renderButton(matrix, mouseX, mouseY, partialTicks);
				this.nbtField.renderButton(matrix, mouseX, mouseY, partialTicks);
			}
		}
		else if(Page.POTIONS.equals(this.page))
		{
			Minecraft.getInstance().font.draw(matrix, (this.potionPage + 1) + "/" + (ForgeRegistries.MOB_EFFECTS.getKeys().size() - 2), x + 118, y - 11, Config.getSkin().getHeadlineColor());
		}
		else if(Page.EQUIPMENT.equals(this.page))
		{
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, BEACON_LOCATION);
	 		container.setBlitOffset(0);
	 		
	 		for(int i = 0; i < 4; i++)
	 		{
		 		if(this.builderSummon.getArmorItem(3 - i).equals(Items.AIR.getRegistryName()))
		 		{
			 		container.blit(matrix, x + 118 + 24 + 2, y + 2 + 24 * i, 112, 221, 16, 16);
		 		}
	 		}
	 		
	 		for(int i = 0; i < 2; i++)
	 		{
		 		if(this.builderSummon.getHandItem(i).equals(Items.AIR.getRegistryName()))
		 		{
			 		container.blit(matrix, x + 118 + 70 + 2 + 24 * i, y + 2 + 36, 112, 221, 16, 16);
		 		}
	 		}
		}
	}
	
	private List<ResourceLocation> sortedPotions()
	{
		return ForgeRegistries.MOB_EFFECTS.getKeys().stream()
				.sorted((a, b) -> I18n.get(ForgeRegistries.MOB_EFFECTS.getValue(a).getDescriptionId()).compareTo(I18n.get(ForgeRegistries.MOB_EFFECTS.getValue(b).getDescriptionId())))
				.collect(Collectors.toList());
	}
	
	private void toggleEditColor(Container container)
	{
		this.editColor = !this.editColor;
		container.init();
	}
	
	private void changePage(Container container, Page page)
	{
		this.page = page;
		container.init();
	}
	
	@Override
	public Category getCategory()
	{
		return Categories.ENTITIES;
	}
	
	@Override
	public MutableComponent getTitle()
	{
		return new TranslatableComponent("gui.worldhandler.title.entities.summon");
	}
	
	@Override
	public MutableComponent getTabTitle()
	{
		return new TranslatableComponent("gui.worldhandler.tab.entities.summon");
	}
	
	@Override
	public Content getActiveContent()
	{
		return Contents.SUMMON;
	}
	
	public static enum Page
	{
		START,
		POTIONS,
		ATTRIBUTES,
		EQUIPMENT;
	}
}
