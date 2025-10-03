# Find all .go files, excluding auto-generated protobuf files.
GO_FILES = $(shell find . -type f -name "*.go" ! -name "*pb.go" ! -name "*pb.grpc.go")

# Phony targets are not real files and will run every time.
.PHONY: all license check-license remove-license clean

# Default command to run when you just type 'make'.
all: license

# Adds the custom license header to all Go source files.
license:
	@echo "==> Applying license headers..."
	@addlicense -c "Aryan Singh" -f ./license-header.txt $(GO_FILES)

# Checks if all Go source files have the correct license header.
check-license:
	@echo "==> Checking license headers..."
	@addlicense -check -c "Aryan Singh" -f ./license-header.txt $(GO_FILES)

# Removes existing license headers from all Go source files using awk.
# It finds the 'package' line and keeps everything from that line to the end of the file.
remove-license:
	@echo "==> Removing existing license headers..."
	@for FILE in $(GO_FILES); do \
		awk '/^package/ {p=1} p' "$$FILE" > "$$FILE.tmp" && mv "$$FILE.tmp" "$$FILE"; \
	done
	@echo "==> Removal complete."

# A standard target to remove generated protobuf files.
clean:
	@echo "==> Cleaning up generated protobuf files..."
	@rm -f ./proto/*.pb.go
	@rm -f ./proto/*.pb.grpc.go