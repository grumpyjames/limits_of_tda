package net.digihippo.bread;

public class OrderQuantityAccumulator {
    private int accumulatedQuantity = 0;

    public void addOrderQuantity(int orderQuantity) {
        accumulatedQuantity += orderQuantity;
    }

    public void placeWholesaleOrder(OutboundEvents events) {
        events.placeWholesaleOrder(accumulatedQuantity);
    }
}
