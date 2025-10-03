package enums

type PollEventType string

const (
	CustomerOrderStatus PollEventType = "CUSTOMER_ORDER_STATUS"
	AdminNewOrder       PollEventType = "ADMIN_NEW_ORDER"
	AdminOrderUpdate    PollEventType = "ADMIN_ORDER_UPDATE"
	InventoryUpdate     PollEventType = "INVENTORY_UPDATE"
	PaymentStatus       PollEventType = "PAYMENT_STATUS"
	SystemNotification  PollEventType = "SYSTEM_NOTIFICATION"
)

type PollActionType string

const (
	Refresh       PollActionType = "REFRESH"
	FetchNew      PollActionType = "FETCH_NEW"
	UpdatePartial PollActionType = "UPDATE_PARTIAL"
	Remove        PollActionType = "REMOVE"
	NoAction      PollActionType = "NO_ACTION"
)

func (p PollEventType) String() string {
	return string(p)
}

func (p PollActionType) String() string {
	return string(p)
}

func IsValidEventType(eventType string) bool {
	switch PollEventType(eventType) {
	case CustomerOrderStatus, AdminNewOrder, AdminOrderUpdate,
		InventoryUpdate, PaymentStatus, SystemNotification:
		return true
	}
	return false
}

func IsValidActionType(actionType string) bool {
	switch PollActionType(actionType) {
	case Refresh, FetchNew, UpdatePartial, Remove, NoAction:
		return true
	}
	return false
}
