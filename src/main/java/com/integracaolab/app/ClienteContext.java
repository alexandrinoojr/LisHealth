package com.integracaolab.app;

import com.integracaolab.app.persistence.entity.Entidade;

public class ClienteContext {

    private static final ThreadLocal<Entidade> CONTEXT =
            new ThreadLocal<>();

    public static void set(Entidade entidade) {
        CONTEXT.set(entidade);
    }

    public static Entidade get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}

