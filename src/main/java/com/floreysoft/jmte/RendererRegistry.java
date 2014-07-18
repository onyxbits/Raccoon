package com.floreysoft.jmte;

import java.util.Collection;

public interface RendererRegistry {

	NamedRenderer resolveNamedRenderer(String rendererName);

	Collection<NamedRenderer> getAllNamedRenderers();

	Collection<NamedRenderer> getCompatibleRenderers(Class<?> inputType);

	Engine deregisterNamedRenderer(NamedRenderer renderer);

	Engine registerNamedRenderer(NamedRenderer renderer);

	<C> Engine registerRenderer(Class<C> clazz, Renderer<C> renderer);

	Engine deregisterRenderer(Class<?> clazz);

	<C> Renderer<C> resolveRendererForClass(Class<C> clazz);

}
