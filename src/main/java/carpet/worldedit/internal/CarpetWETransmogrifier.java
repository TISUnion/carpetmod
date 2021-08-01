/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package carpet.worldedit.internal;

import carpet.worldedit.CarpetWEAdapter;
import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.registry.state.*;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.block.BlockType;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.IStringSerializable;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Raw, un-cached transformations.
 */
public class CarpetWETransmogrifier
{

    public static Property<?> transmogToWorldEditProperty(net.minecraft.state.IProperty<?> property) {
        if (property instanceof net.minecraft.state.BooleanProperty) {
            return new BooleanProperty(property.getName(), ImmutableList.copyOf(((net.minecraft.state.BooleanProperty) property).getAllowedValues()));
        }
        if (property instanceof net.minecraft.state.IntegerProperty) {
            return new IntegerProperty(property.getName(), ImmutableList.copyOf(((net.minecraft.state.IntegerProperty) property).getAllowedValues()));
        }
        if (property instanceof DirectionProperty) {
            return new DirectionalProperty(property.getName(), ((DirectionProperty) property).getAllowedValues().stream()
                .map(CarpetWEAdapter::adaptEnumFacing)
                .collect(Collectors.toList()));
        }
        if (property instanceof net.minecraft.state.EnumProperty) {
            // Note: do not make x.asString a method reference.
            // It will cause runtime bootstrap exceptions.
            return new EnumProperty(property.getName(), ((net.minecraft.state.EnumProperty<?>) property).getAllowedValues().stream()
                .map(x -> x.getName())
                .collect(Collectors.toList()));
        }
        return new PropertyAdapter<>(property);
    }

    private static Map<Property<?>, Object> transmogToWorldEditProperties(BlockType block, Map<net.minecraft.state.IProperty<?>, Comparable<?>> mcProps) {
        Map<Property<?>, Object> props = new TreeMap<>(Comparator.comparing(Property::getName));
        for (Map.Entry<net.minecraft.state.IProperty<?>, Comparable<?>> prop : mcProps.entrySet()) {
            Object value = prop.getValue();
            if (prop.getKey() instanceof DirectionProperty) {
                value = CarpetWEAdapter.adaptEnumFacing((net.minecraft.util.EnumFacing) value);
            } else if (prop.getKey() instanceof net.minecraft.state.EnumProperty) {
                value = ((IStringSerializable) value).getName();
            }
            props.put(block.getProperty(prop.getKey().getName()), value);
        }
        return props;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static net.minecraft.block.state.IBlockState transmogToMinecraftProperties(StateContainer<Block, IBlockState> stateContainer,
                                                                                       net.minecraft.block.state.IBlockState newState, Map<Property<?>, Object> states) {
        for (Map.Entry<Property<?>, Object> state : states.entrySet()) {
            net.minecraft.state.IProperty property = stateContainer.getProperty(state.getKey().getName());
            Comparable value = (Comparable) state.getValue();
            // we may need to adapt this value, depending on the source prop
            if (property instanceof DirectionProperty) {
                Direction dir = (Direction) value;
                value = CarpetWEAdapter.adapt(dir);
            } else if (property instanceof net.minecraft.state.EnumProperty) {
                String enumName = (String) value;
                value = ((net.minecraft.state.EnumProperty<?>) property).parseValue((String) value).orElseGet(() -> {
                    throw new IllegalStateException("Enum property " + property.getName() + " does not contain " + enumName);
                });
            }

            newState = newState.with(property, value);
        }
        return newState;
    }

    public static net.minecraft.block.state.IBlockState transmogToMinecraft(com.sk89q.worldedit.world.block.BlockState blockState) {
        Block mcBlock = CarpetWEAdapter.adapt(blockState.getBlockType());
        net.minecraft.block.state.IBlockState newState = mcBlock.getDefaultState();
        Map<Property<?>, Object> states = blockState.getStates();
        return transmogToMinecraftProperties(mcBlock.getStateContainer(), newState, states);
    }

    public static com.sk89q.worldedit.world.block.BlockState transmogToWorldEdit(net.minecraft.block.state.IBlockState blockState) {
        BlockType blockType = CarpetWEAdapter.adapt(blockState.getBlock());
        return blockType.getState(transmogToWorldEditProperties(blockType, blockState.getValues()));
    }

    private CarpetWETransmogrifier() {
    }
}
