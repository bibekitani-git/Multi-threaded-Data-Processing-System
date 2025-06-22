// --- Go Implementation ---

package main

import (
	"fmt"
	"log"
	"os"
	"sync"
	"time"
)

// Task struct represents a unit of work
type Task struct {
	id int
}

// worker processes tasks received from the tasks channel
func worker(id int, tasks <-chan Task, results *[]string, wg *sync.WaitGroup, mu *sync.Mutex) {
	defer wg.Done() // ensure WaitGroup is decremented when done
	for task := range tasks {
		log.Printf("Worker %d processing task %d", id, task.id)
		time.Sleep(500 * time.Millisecond) // simulate work delay

		// Lock before writing to shared results slice
		mu.Lock()
		*results = append(*results, fmt.Sprintf("Worker %d processed task %d", id, task.id))
		mu.Unlock()
	}
}

func main() {
	// Create a buffered channel for tasks
	tasks := make(chan Task, 10)
	var results []string
	var wg sync.WaitGroup
	var mu sync.Mutex

	// Enqueue 10 tasks
	for i := 1; i <= 10; i++ {
		tasks <- Task{id: i}
	}
	close(tasks) // no more tasks will be added

	// Start 4 worker goroutines
	for i := 0; i < 4; i++ {
		wg.Add(1)
		go worker(i, tasks, &results, &wg, &mu)
	}

	// Wait for all workers to complete
	wg.Wait()

	// Write results to output file
	file, err := os.Create("output_go.txt")
	if err != nil {
		log.Fatalf("Error creating file: %v", err)
	}
	defer file.Close()

	for _, line := range results {
		_, err := file.WriteString(line + "\n")
		if err != nil {
			log.Printf("Error writing to file: %v", err)
		}
	}
}
