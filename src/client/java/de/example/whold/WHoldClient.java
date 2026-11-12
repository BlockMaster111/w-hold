package de.example.whold;

import com.mojang.brigadier.arguments.FloatArgumentType;
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
private static double pendingDuration = 0.0;
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(
    literal("w")
        .then(argument("laufzeit", DoubleArgumentType.doubleArg(0.05, 3600.0))
            .then(argument("verzoegerung", DoubleArgumentType.doubleArg(0.0, 3600.0))
                .executes(context -> {
                    double duration = DoubleArgumentType.getDouble(context, "laufzeit");
                    double delay = DoubleArgumentType.getDouble(context, "verzoegerung");

                    startHolding(duration, delay);

                    context.getSource().sendFeedback(
                        Component.literal(
                            "W startet in " + delay + " Sekunden und wird " +
                            duration + " Sekunden gehalten."
                        )
                    );
                    return 1;
                })
            )
        )
);
    }

    private static void startHolding(double seconds, double delay) {
    Minecraft client = Minecraft.getInstance();

    client.options.keyUp.setDown(false);

    long now = System.nanoTime();

    startAtNanos = now + (long) (delay * 1_000_000_000L);
    releaseAtNanos = startAtNanos + (long) (seconds * 1_000_000_000L);

    pendingDuration = seconds;
    holdingForward = false;
}
}
