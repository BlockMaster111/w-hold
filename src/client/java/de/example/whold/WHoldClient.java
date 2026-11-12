package de.example.whold;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

public final class WHoldClient implements ClientModInitializer {

    private static long startAtNanos = 0L;
    private static long releaseAtNanos = 0L;
    private static boolean holdingForward = false;

    private static long leftStartAtNanos = 0L;
    private static long leftReleaseAtNanos = 0L;
    private static boolean holdingLeft = false;

    private static long sneakStartAtNanos = 0L;
    private static long sneakReleaseAtNanos = 0L;
    private static boolean holdingSneak = false;

    @Override
    public void onInitializeClient() {

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {

            // /w <laufzeit> <verzoegerung>
            dispatcher.register(
                literal("w")
                    .then(argument("laufzeit", DoubleArgumentType.doubleArg(0.05, 3600.0))
                        .then(argument("verzoegerung", DoubleArgumentType.doubleArg(0.0, 3600.0))
                            .executes(context -> {
                                double duration = DoubleArgumentType.getDouble(context, "laufzeit");
                                double delay = DoubleArgumentType.getDouble(context, "verzoegerung");

                                startHolding(duration, delay);

                                context.getSource().sendFeedback(
                                    Component.literal("W startet in " + delay +
                                        " Sekunden und wird " + duration + " Sekunden gehalten.")
                                );

                                return 1;
                            })
                        )
                    )
            );

            // /a <laufzeit> <verzoegerung>
            dispatcher.register(
                literal("a")
                    .then(argument("laufzeit", DoubleArgumentType.doubleArg(0.05, 3600.0))
                        .then(argument("verzoegerung", DoubleArgumentType.doubleArg(0.0, 3600.0))
                            .executes(context -> {
                                double duration = DoubleArgumentType.getDouble(context, "laufzeit");
                                double delay = DoubleArgumentType.getDouble(context, "verzoegerung");

                                startHoldingLeft(duration, delay);

                                context.getSource().sendFeedback(
                                    Component.literal("A startet in " + delay +
                                        " Sekunden und wird " + duration + " Sekunden gehalten.")
                                );

                                return 1;
                            })
                        )
                    )
            );

            // /sneak <laufzeit> <verzoegerung>
            dispatcher.register(
                literal("sneak")
                    .then(argument("laufzeit", DoubleArgumentType.doubleArg(0.05, 3600.0))
                        .then(argument("verzoegerung", DoubleArgumentType.doubleArg(0.0, 3600.0))
                            .executes(context -> {
                                double duration = DoubleArgumentType.getDouble(context, "laufzeit");
                                double delay = DoubleArgumentType.getDouble(context, "verzoegerung");

                                startSneaking(duration, delay);

                                context.getSource().sendFeedback(
                                    Component.literal("Sneaken startet in " + delay +
                                        " Sekunden und dauert " + duration + " Sekunden.")
                                );

                                return 1;
                            })
                        )
                    )
            );
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            long now = System.nanoTime();

            if (client.player == null) {
                client.options.keyUp.setDown(false);
                client.options.keySprint.setDown(false);
                client.options.keyLeft.setDown(false);
                client.options.keyShift.setDown(false);

                holdingForward = false;
                holdingLeft = false;
                holdingSneak = false;
                startAtNanos = 0L;
                leftStartAtNanos = 0L;
                sneakStartAtNanos = 0L;
                return;
            }

            // W + Sprint
            if (!holdingForward && startAtNanos > 0L && now >= startAtNanos) {
                client.options.keyUp.setDown(true);
                client.options.keySprint.setDown(true);
                holdingForward = true;
            }

            if (holdingForward && now >= releaseAtNanos) {
                client.options.keyUp.setDown(false);
                client.options.keySprint.setDown(false);
                holdingForward = false;
                startAtNanos = 0L;
            }

            // A
            if (!holdingLeft && leftStartAtNanos > 0L && now >= leftStartAtNanos) {
                client.options.keyLeft.setDown(true);
                holdingLeft = true;
            }

            if (holdingLeft && now >= leftReleaseAtNanos) {
                client.options.keyLeft.setDown(false);
                holdingLeft = false;
                leftStartAtNanos = 0L;
            }

            // Sneak
            if (!holdingSneak && sneakStartAtNanos > 0L && now >= sneakStartAtNanos) {
                client.options.keyShift.setDown(true);
                holdingSneak = true;
            }

            if (holdingSneak && now >= sneakReleaseAtNanos) {
                client.options.keyShift.setDown(false);
                holdingSneak = false;
                sneakStartAtNanos = 0L;
            }
        });
    }

    private static void startHolding(double seconds, double delay) {
        Minecraft client = Minecraft.getInstance();

        if (holdingForward) {
            client.options.keyUp.setDown(false);
            client.options.keySprint.setDown(false);
            holdingForward = false;
        }

        long now = System.nanoTime();

        startAtNanos = now + (long) (delay * 1_000_000_000L);
        releaseAtNanos = startAtNanos + (long) (seconds * 1_000_000_000L);
    }

    private static void startHoldingLeft(double seconds, double delay) {
        Minecraft client = Minecraft.getInstance();

        if (holdingLeft) {
            client.options.keyLeft.setDown(false);
            holdingLeft = false;
        }

        long now = System.nanoTime();

        leftStartAtNanos = now + (long) (delay * 1_000_000_000L);
        leftReleaseAtNanos = leftStartAtNanos + (long) (seconds * 1_000_000_000L);
    }

    private static void startSneaking(double seconds, double delay) {
        Minecraft client = Minecraft.getInstance();

        if (holdingSneak) {
            client.options.keyShift.setDown(false);
            holdingSneak = false;
        }

        long now = System.nanoTime();

        sneakStartAtNanos = now + (long) (delay * 1_000_000_000L);
        sneakReleaseAtNanos = sneakStartAtNanos +
            (long) (seconds * 1_000_000_000L);
    }
}