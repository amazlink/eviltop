package io.github.rothschil.design.abstractfactory;

import io.github.rothschil.design.factory.Steel;

public abstract class AbstractFactory {

    public abstract Color getColor(int type);
    public abstract Steel getSteel(int type);
}
