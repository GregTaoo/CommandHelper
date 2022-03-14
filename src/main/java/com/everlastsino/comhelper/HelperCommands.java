package com.everlastsino.comhelper;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class HelperCommands {
    private static final Map<PlayerEntity, PlayerEntity> TP_REQUESTS = new HashMap<>();
    private static final SimpleCommandExceptionType INVALID_POSITION_EXCEPTION = new SimpleCommandExceptionType(new TranslatableText("commands.teleport.invalidPosition"));
    private static final List<String> EASTER_EGGS = List.of(
            "Oh man",
            "Creepy?",
            "OMICRON WARNING, TAKE CARE.",
            "FBI OPEN THE DOOR",
            "Made in China!",
            "114514, 1919810",
            "Why do you know my NAME?",
            "LMAO!",
            "OUCH! Is the server overloaded? @!@",
            "Every minutes count. So what R U doing?",
            "gg",
            "Hello world ~",
            "It's none of my business"
    );
    private static int EGG_COUNT = 0;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, Logger logger) {
        dispatcher.register(literal("btpa")
                .then(literal("player")
                        .then(argument("destination", EntityArgumentType.player())
                                .executes((context) -> {
                                    if (!CHConfig.playerTPAgreementRequirement) {
                                        return teleportPlayer(context.getSource(), context.getSource().getPlayer(),
                                                EntityArgumentType.getPlayer(context, "destination"));
                                    } else {
                                        TP_REQUESTS.remove(EntityArgumentType.getPlayer(context, "destination"));
                                        TP_REQUESTS.put(EntityArgumentType.getPlayer(context, "destination"), context.getSource().getPlayer());
                                        context.getSource().getPlayer().sendMessage(
                                                Text.of(String.format(CHConfig.sendTPRequest,
                                                        EntityArgumentType.getPlayer(context, "destination").getDisplayName().getString())), false);
                                        EntityArgumentType.getPlayer(context, "destination").sendMessage(
                                                Text.of(String.format("\n====================\n%s\n====================\n", String.format(CHConfig.receiveTPRequest,
                                                        context.getSource().getPlayer().getDisplayName().getString())))
                                                        .getWithStyle(Style.EMPTY.withColor(Formatting.AQUA)).get(0), false);
                                        EntityArgumentType.getPlayer(context, "destination").sendMessage(
                                                Text.of(String.format(CHConfig.receiveTPRequest,
                                                                context.getSource().getPlayer().getDisplayName().getString()))
                                                        .getWithStyle(Style.EMPTY.withColor(Formatting.YELLOW)).get(0), true);
                                        EntityArgumentType.getPlayer(context, "destination").playSound(SoundEvents.ENTITY_GHAST_WARN,
                                                SoundCategory.PLAYERS, 2.0F, 1.0F);
                                    }
                                    return 1;
                                })
                        )
                )
                .then(literal("accept")
                        .executes((context) -> {
                            PlayerEntity target = TP_REQUESTS.getOrDefault(context.getSource().getPlayer(), null);
                            if (target == null) {
                                context.getSource().getPlayer().sendMessage(Text.of(CHConfig.targetNotFound), false);
                                return 1;
                            }
                            teleportPlayer(context.getSource(), target, context.getSource().getPlayer());
                            TP_REQUESTS.remove(context.getSource().getPlayer());
                            target.sendMessage(Text.of(String.format(CHConfig.requestBeAgreed,
                                    context.getSource().getPlayer().getDisplayName().getString()))
                                    .getWithStyle(Style.EMPTY.withColor(Formatting.AQUA)).get(0), false);
                            return 1;
                        })
                )
        );
        dispatcher.register(literal("at").requires((source) -> CHConfig.playerAtAllows)
                .then(argument("target", EntityArgumentType.players())
                    .then(argument("color", ColorArgumentType.color())
                            .then(literal("no_sound")
                                    .executes((context) -> atPlayerWithColor(context, false))
                            ).then(literal("with_sound")
                                    .executes((context) -> atPlayerWithColor(context, true))
                            ).executes((context) -> atPlayerWithColor(context, true))
                    ).executes(HelperCommands::atPlayerDefault)
                )
        );
        dispatcher.register(literal("comhelper")
                .then(literal("reload").requires((source) -> source.hasPermissionLevel(2))
                        .executes((context -> {
                            logger.info("Configs are reloading.");
                            try {
                                boolean success = CHConfig.loadConfigs();
                                context.getSource().getPlayer().sendMessage(
                                        Text.of(success ? CHConfig.configReloaded : CHConfig.configReloadFailed)
                                                .getWithStyle(Style.EMPTY.withColor(success ? Formatting.YELLOW : Formatting.RED)).get(0),
                                        false
                                );
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return 1;
                        }))
                )
                .then(literal("lang").requires((source) -> source.hasPermissionLevel(2))
                        .then(argument("language", StringArgumentType.word())
                                .executes((context -> {
                                    String str = StringArgumentType.getString(context, "language");
                                    if (str.length() != 5 || str.charAt(2) != '_') {
                                        context.getSource().getPlayer().sendMessage(
                                                Text.of(CHConfig.formatError)
                                                        .getWithStyle(Style.EMPTY.withColor(Formatting.RED)).get(0),
                                                false
                                        );
                                        return 1;
                                    }
                                    try {
                                        boolean success = CHConfig.setLanguage(StringArgumentType.getString(context, "language"));
                                        context.getSource().getPlayer().sendMessage(
                                                Text.of(success ? CHConfig.languageChanged : CHConfig.languageChangeFailed)
                                                        .getWithStyle(Style.EMPTY.withColor(success ? Formatting.YELLOW : Formatting.RED)).get(0),
                                                false
                                        );
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return 1;
                                }))
                        )
                )
                .then(literal("about")
                        .executes((context -> {
                            context.getSource().getPlayer().sendMessage(
                                    Text.of(CHConfig.aboutThisMod)
                                            .getWithStyle(Style.EMPTY.withColor(Formatting.AQUA)).get(0),
                                    false
                            );
                            return 1;
                        }))
                )
                .then(literal("donate")
                        .executes((context) -> {
                            try {
                                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler https://afdian.net/@gregtao");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return 1;
                        })
                )
                .then(argument("option", StringArgumentType.word())
                        .executes((context) -> {
                            String str = StringArgumentType.getString(context, "option");
                            if (str.equals("GregTao") || str.equals("gregtao")) {
                                EGG_COUNT++;
                                if (EGG_COUNT == 100) {
                                    context.getSource().getPlayer().sendMessage(
                                            Text.of("Congratulations that you've gotten 100 likes! TAT")
                                                    .getWithStyle(Style.EMPTY.withColor(Formatting.AQUA)).get(0),
                                            false
                                    );
                                    EGG_COUNT = 0;
                                    return 1;
                                }
                                Random random = new Random();
                                context.getSource().getPlayer().sendMessage(
                                        Text.of(random.nextInt(10) == 0 ? EASTER_EGGS.get(random.nextInt(6)) : "What's up? " + (EGG_COUNT >= 50 ? EGG_COUNT : ""))
                                                .getWithStyle(Style.EMPTY.withColor(Formatting.AQUA)).get(0),
                                        false
                                );
                            }
                            return 1;
                        })
                )
                .then(literal("help")
                        .executes((context) -> {
                            context.getSource().getPlayer().sendMessage(
                                    Text.of(CHConfig.help),
                                    false
                            );
                            return 1;
                        })
                )
        );
    }

    public static int atPlayer(CommandContext<ServerCommandSource> context, boolean sound, Formatting color) throws CommandSyntaxException{
        if (sound) {
            EntityArgumentType.getPlayer(context, "target").playSound(SoundEvents.ENTITY_GHAST_WARN, SoundCategory.PLAYERS, 2.0F, 1.0F);
        }
        EntityArgumentType.getPlayer(context, "target")
                .sendMessage(Text.of(String.format(CHConfig.atReminding,
                        context.getSource().getPlayer().getDisplayName().getString())).getWithStyle(Style.EMPTY.withColor(color)).get(0), true);
        EntityArgumentType.getPlayer(context, "target")
                .sendMessage(Text.of(String.format("\n====================\n%s\n====================\n", String.format(CHConfig.atReminding,
                        context.getSource().getPlayer().getDisplayName().getString()))).getWithStyle(Style.EMPTY.withColor(color)).get(0), false);
        return 1;
    }

    public static int atPlayerWithColor(CommandContext<ServerCommandSource> context, boolean sound) throws CommandSyntaxException{
        return atPlayer(context, sound, ColorArgumentType.getColor(context,"color"));
    }

    public static int atPlayerDefault(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
        return atPlayer(context, true, Formatting.WHITE);
    }

    public static int teleportPlayer(ServerCommandSource source, PlayerEntity target, PlayerEntity destination) throws CommandSyntaxException {
        teleport(target, (ServerWorld) destination.world, destination.getX(), destination.getY(), destination.getZ(),
                destination.getYaw(), destination.getPitch());
        source.sendFeedback(new TranslatableText("commands.teleport.success.entity.single",
                target.getDisplayName(), destination.getDisplayName()), true);
        return 1;
    }

    private static void teleport(Entity target, ServerWorld world, double x, double y, double z, float yaw, float pitch) throws CommandSyntaxException {
        BlockPos blockPos = new BlockPos(x, y, z);
        if (!World.isValid(blockPos)) {
            throw INVALID_POSITION_EXCEPTION.create();
        } else {
            float f = MathHelper.wrapDegrees(yaw);
            float g = MathHelper.wrapDegrees(pitch);
            if (target instanceof ServerPlayerEntity) {
                ChunkPos chunkPos = new ChunkPos(new BlockPos(x, y, z));
                world.getChunkManager().addTicket(ChunkTicketType.POST_TELEPORT, chunkPos, 1, target.getId());
                target.stopRiding();
                if (((ServerPlayerEntity)target).isSleeping()) {
                    ((ServerPlayerEntity)target).wakeUp(true, true);
                }
                if (world == target.world) {
                    ((ServerPlayerEntity)target).networkHandler.requestTeleport(x, y, z, f, g);
                } else {
                    ((ServerPlayerEntity)target).teleport(world, x, y, z, f, g);
                }

                target.setHeadYaw(f);
            } else {
                float chunkPos = MathHelper.clamp(g, -90.0F, 90.0F);
                if (world == target.world) {
                    target.refreshPositionAndAngles(x, y, z, f, chunkPos);
                    target.setHeadYaw(f);
                } else {
                    target.detach();
                    Entity entity = target;
                    target = target.getType().create(world);
                    if (target == null) {
                        return;
                    }

                    target.copyFrom(entity);
                    target.refreshPositionAndAngles(x, y, z, f, chunkPos);
                    target.setHeadYaw(f);
                    entity.setRemoved(Entity.RemovalReason.CHANGED_DIMENSION);
                    world.onDimensionChanged(target);
                }
            }

            if (!(target instanceof LivingEntity) || !((LivingEntity)target).isFallFlying()) {
                target.setVelocity(target.getVelocity().multiply(1.0D, 0.0D, 1.0D));
                target.setOnGround(true);
            }

            if (target instanceof PathAwareEntity) {
                ((PathAwareEntity)target).getNavigation().stop();
            }

        }
    }

}