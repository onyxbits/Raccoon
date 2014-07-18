package com.floreysoft.jmte.renderer;

import com.floreysoft.jmte.NamedRenderer;
import com.floreysoft.jmte.encoder.Encoder;

/**
 * <p>Marker interface to indicate that the result of a renderer implementing this
 * interface shall not be encoded by any {@link Encoder} that might be
 * configured.</p>
 * 
 * Can be set on {@link Renderer} and {@link NamedRenderer}.
 * 
 * @see Encoder
 * @see NamedRenderer
 * @see Renderer
 */
public interface RawRenderer {

}
