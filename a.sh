#!/bin/bash
echo "Repository name:"
read repo_name

git init
git add .
git commit -m "Initial commit"
gh repo create "$repo_name" --public --source=. --remote=origin --push

echo "✅ Done! https://github.com/$(gh api user -q .login)/$repo_name"
