#!/bin/bash

# Find all Java files in the src directory
files=$(find src -type f -name "*.java")

# For each file, remove trailing whitespace
for file in $files; do
    # Create a temporary file
    temp_file=$(mktemp)
    
    # Remove trailing whitespace and write to temporary file
    sed 's/[[:space:]]*$//' "$file" > "$temp_file"
    
    # Replace the original file with the temporary file
    mv "$temp_file" "$file"
    
    echo "Processed $file"
done

echo "Done removing trailing whitespace from all Java files in src directory."