package com.simibubi.create.content.logistics.trains.management;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.trains.ITrackBlock;
import com.simibubi.create.content.logistics.trains.TrackGraphHelper;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class TrackTargetingBlockItem extends BlockItem {

	public TrackTargetingBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Override
	public InteractionResult useOn(UseOnContext pContext) {
		ItemStack stack = pContext.getItemInHand();
		BlockPos pos = pContext.getClickedPos();
		Level level = pContext.getLevel();
		BlockState state = level.getBlockState(pos);
		Player player = pContext.getPlayer();

		if (player == null)
			return InteractionResult.FAIL;

		if (player.isSteppingCarefully() && stack.hasTag()) {
			if (level.isClientSide)
				return InteractionResult.SUCCESS;
			player.displayClientMessage(Lang.translate("track_target.clear"), true);
			stack.setTag(null);
			return InteractionResult.SUCCESS;
		}

		if (state.getBlock()instanceof ITrackBlock track) {
			if (level.isClientSide)
				return InteractionResult.SUCCESS;
			CompoundTag stackTag = stack.getOrCreateTag();
			Vec3 lookAngle = player.getLookAngle();
			boolean front = TrackGraphHelper.getTrackDirectionByLookVec(lookAngle, level, pos, state, track);
			stackTag.put("SelectedPos", NbtUtils.writeBlockPos(pos));
			stackTag.putBoolean("SelectedDirection", front);
			player.displayClientMessage(Lang.translate("track_target.set"), true);
			stack.setTag(stackTag);
			return InteractionResult.SUCCESS;
		}

		if (!stack.hasTag()) {
			player.displayClientMessage(Lang.translate("track_target.missing")
				.withStyle(ChatFormatting.RED), true);
			return InteractionResult.FAIL;
		}

		CompoundTag tag = stack.getTag();
		CompoundTag teTag = new CompoundTag();
		teTag.putBoolean("TargetDirection", tag.getBoolean("SelectedDirection"));

		BlockPos selectedPos = NbtUtils.readBlockPos(tag.getCompound("SelectedPos"));
		BlockPos placedPos = pos.relative(pContext.getClickedFace(), state.getMaterial()
			.isReplaceable() ? 0 : 1);

		if (!selectedPos.closerThan(placedPos, 16)) {
			player.displayClientMessage(Lang.translate("track_target.too_far")
				.withStyle(ChatFormatting.RED), true);
			return InteractionResult.FAIL;
		}

		teTag.put("TargetTrack", NbtUtils.writeBlockPos(selectedPos.subtract(placedPos)));
		tag.put("BlockEntityTag", teTag);

		InteractionResult useOn = super.useOn(pContext);
		if (level.isClientSide || useOn == InteractionResult.FAIL)
			return useOn;

		ItemStack itemInHand = player.getItemInHand(pContext.getHand());
		if (!itemInHand.isEmpty())
			itemInHand.setTag(null);
		player.displayClientMessage(Lang.translate("track_target.success")
			.withStyle(ChatFormatting.GREEN), true);
		return useOn;
	}

	public static void clientTick() {

	}

	public static void render(PoseStack ms, SuperRenderTypeBuffer buffer) {

	}

}