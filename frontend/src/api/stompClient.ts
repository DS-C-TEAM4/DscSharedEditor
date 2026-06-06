import { Client, IMessage, StompSubscription } from "@stomp/stompjs";
import { SOCKET_URL } from "./config";

type MessageHandler<T> = (message: T) => void;

class StompClientManager {
  private client: Client | null = null;
  private subscriptions = new Map<string, StompSubscription>();

  connect(onConnect?: () => void, onError?: (error: unknown) => void) {
    if (this.client?.connected) {
      onConnect?.();
      return;
    }

    this.client = new Client({
      brokerURL: SOCKET_URL,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        onConnect?.();
      },
      onStompError: (frame) => {
        onError?.(frame);
      },
      onWebSocketError: (event) => {
        onError?.(event);
      },
    });

    this.client.activate();
  }

  disconnect() {
    this.subscriptions.forEach((subscription) => subscription.unsubscribe());
    this.subscriptions.clear();
    this.client?.deactivate();
    this.client = null;
  }

  publish(destination: string, body: unknown) {
    if (!this.client?.connected) {
      throw new Error("STOMP client is not connected");
    }

    this.client.publish({
      destination,
      body: JSON.stringify(body),
    });
  }

  subscribe<T>(destination: string, handler: MessageHandler<T>) {
    if (!this.client?.connected) {
      throw new Error("STOMP client is not connected");
    }

    const subscription = this.client.subscribe(
      destination,
      (message: IMessage) => {
        handler(JSON.parse(message.body) as T);
      },
    );

    const subscriptionId = `${destination}-${Date.now()}-${Math.random()}`;
    this.subscriptions.set(subscriptionId, subscription);

    return subscriptionId;
  }

  unsubscribe(subscriptionId: string) {
    this.subscriptions.get(subscriptionId)?.unsubscribe();
    this.subscriptions.delete(subscriptionId);
  }

  isConnected() {
    return Boolean(this.client?.connected);
  }
}

export const stompClient = new StompClientManager();
