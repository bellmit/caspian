package com.emc.caspian.ccs.imagerepo.api;

/**
 * @author shivesh
 */
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.NameBinding;

/**
 * HTTP "PATCH" method annotation. The annotation acts at the same time as JAX-RS
 * filter/intercepter, i.e. it can be applied to custom filter/intercepter that implements the PATCH
 * support and JAX-RS runtime will take care of automatically associating the filter/intercepter
 * with the annotated resource method.
 *
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@HttpMethod("PATCH")
@Documented
@NameBinding
public @interface PATCH {
}

