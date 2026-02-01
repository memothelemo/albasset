package xyz.memothelemo.albasset.mixin;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.ParsedCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.PermissionSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.memothelemo.albasset.callback.AdminCommands;

@Mixin(Commands.class)
public abstract class CommandsMixin {
    @Shadow
    public abstract CommandDispatcher<CommandSourceStack> getDispatcher();

    @Inject(method = "performCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/commands/Commands;executeCommandInContext(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/function/Consumer;)V"))
    private void capturePrivilegedCommandUse(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfo ci) {
        CommandSourceStack source = parseResults.getContext().getSource();

        // Assume that `checkCommand` successfully executed
        ParseResults<CommandSourceStack> results = this.getDispatcher().parse(command, source);

        // This is to determine whether if the command used is for admins only
        ParsedCommandNode<CommandSourceStack> head = results.getContext().getNodes().getFirst();

        // Therefore this command is not for everyone
        if (head != null && head.getNode().canUse(source.withPermission(PermissionSet.NO_PERMISSIONS))) return;
        AdminCommands.ExecuteCallback.EVENT.invoker().executed(source, command);
    }
}
