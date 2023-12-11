package main

import (
	"context"
	"fmt"

	"github.com/aws/aws-lambda-go/lambda"
)

type MyEvent struct {
	Name string `json:"name"`
}

func tes(ctx context.Context, name MyEvent) (string, error) {
	return fmt.Sprintf("Hello sample1%s!", name.Name), nil
}
func main() {
	lambda.Start(tes)
}