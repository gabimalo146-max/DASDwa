package me.Gui.gui.mixin;

import me.Gui.gui.modules.NameProtectUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={TextRenderer.class})
public class TextRendererMixin {
    @ModifyVariable(method={"draw(Ljava/lang/String;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I"}, at=@At(value="HEAD"), argsOnly=true)
    private String gui$nameProtectDrawString(String text) {
        return NameProtectUtil.replaceString(text);
    }

    @ModifyVariable(method={"draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I"}, at=@At(value="HEAD"), argsOnly=true)
    private Text gui$nameProtectDrawText(Text text) {
        return NameProtectUtil.replaceText(text);
    }

    @ModifyVariable(method={"draw(Lnet/minecraft/text/Text;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;IIZ)I"}, at=@At(value="HEAD"), argsOnly=true)
    private Text gui$nameProtectDrawTextZ(Text text) {
        return NameProtectUtil.replaceText(text);
    }

    @ModifyVariable(method={"draw(Lnet/minecraft/text/OrderedText;FFIZLorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/font/TextRenderer$TextLayerType;II)I"}, at=@At(value="HEAD"), argsOnly=true)
    private OrderedText gui$nameProtectDrawOrdered(OrderedText text) {
        return NameProtectUtil.replaceOrderedText(text);
    }

    @ModifyVariable(method={"drawWithOutline(Lnet/minecraft/text/OrderedText;FFIILorg/joml/Matrix4f;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"}, at=@At(value="HEAD"), argsOnly=true)
    private OrderedText gui$nameProtectDrawOutline(OrderedText text) {
        return NameProtectUtil.replaceOrderedText(text);
    }

    @ModifyVariable(method={"getWidth(Ljava/lang/String;)I"}, at=@At(value="HEAD"), argsOnly=true)
    private String gui$nameProtectWidthString(String text) {
        return NameProtectUtil.replaceString(text);
    }

    @ModifyVariable(method={"getWidth(Lnet/minecraft/text/StringVisitable;)I"}, at=@At(value="HEAD"), argsOnly=true)
    private StringVisitable gui$nameProtectWidthVisitable(StringVisitable text) {
        return NameProtectUtil.replaceVisitable(text);
    }

    @ModifyVariable(method={"getWidth(Lnet/minecraft/text/OrderedText;)I"}, at=@At(value="HEAD"), argsOnly=true)
    private OrderedText gui$nameProtectWidthOrdered(OrderedText text) {
        return NameProtectUtil.replaceOrderedText(text);
    }

    @ModifyVariable(method={"trimToWidth(Ljava/lang/String;IZ)Ljava/lang/String;"}, at=@At(value="HEAD"), argsOnly=true)
    private String gui$nameProtectTrimString(String text) {
        return NameProtectUtil.replaceString(text);
    }

    @ModifyVariable(method={"trimToWidth(Ljava/lang/String;I)Ljava/lang/String;"}, at=@At(value="HEAD"), argsOnly=true)
    private String gui$nameProtectTrimString2(String text) {
        return NameProtectUtil.replaceString(text);
    }

    @ModifyVariable(method={"trimToWidth(Lnet/minecraft/text/StringVisitable;I)Lnet/minecraft/text/StringVisitable;"}, at=@At(value="HEAD"), argsOnly=true)
    private StringVisitable gui$nameProtectTrimVisitable(StringVisitable text) {
        return NameProtectUtil.replaceVisitable(text);
    }

    @ModifyVariable(method={"getWrappedLinesHeight(Ljava/lang/String;I)I"}, at=@At(value="HEAD"), argsOnly=true)
    private String gui$nameProtectWrappedHeightString(String text) {
        return NameProtectUtil.replaceString(text);
    }

    @ModifyVariable(method={"getWrappedLinesHeight(Lnet/minecraft/text/StringVisitable;I)I"}, at=@At(value="HEAD"), argsOnly=true)
    private StringVisitable gui$nameProtectWrappedHeightVisitable(StringVisitable text) {
        return NameProtectUtil.replaceVisitable(text);
    }

    @ModifyVariable(method={"wrapLines(Lnet/minecraft/text/StringVisitable;I)Ljava/util/List;"}, at=@At(value="HEAD"), argsOnly=true)
    private StringVisitable gui$nameProtectWrapLines(StringVisitable text) {
        return NameProtectUtil.replaceVisitable(text);
    }
}

