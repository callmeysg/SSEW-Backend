package grpc

import (
	"context"

	"github.com/callmeysg/SSEW-Backend/telemetry-service/internal/service"
	pb "github.com/callmeysg/SSEW-Backend/telemetry-service/proto"
	"google.golang.org/protobuf/types/known/emptypb"
)

type TelemetryServer struct {
	pb.UnimplementedTelemetryServiceServer
	eventService service.EventService
}

func NewTelemetryServer(eventService service.EventService) *TelemetryServer {
	return &TelemetryServer{
		eventService: eventService,
	}
}

func (s *TelemetryServer) PublishOrderStatusChange(ctx context.Context, req *pb.PublishOrderStatusChangeRequest) (*emptypb.Empty, error) {
	err := s.eventService.PublishOrderStatusChangeEvent(ctx, req.OrderId, req.UserId, req.NewStatus)
	return &emptypb.Empty{}, err
}

func (s *TelemetryServer) PublishNewOrder(ctx context.Context, req *pb.PublishNewOrderRequest) (*emptypb.Empty, error) {
	err := s.eventService.PublishNewOrderEventForAdmin(ctx, req.OrderId, req.CustomerName, req.TotalAmount)
	return &emptypb.Empty{}, err
}

func (s *TelemetryServer) PublishOrderUpdate(ctx context.Context, req *pb.PublishOrderUpdateRequest) (*emptypb.Empty, error) {
	detailsMap := req.Details.AsMap()
	err := s.eventService.PublishOrderUpdateEventForAdmin(ctx, req.OrderId, req.UpdateType, detailsMap)
	return &emptypb.Empty{}, err
}
