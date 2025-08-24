# Contributing to Rate Limiter

Thanks for your interest in contributing 🎉!

We welcome issues, feature requests, and pull requests. To keep things smooth:

## 🐛 Reporting Issues
- Use the GitHub [Issues](https://github.com/amythlabs/rate-limiter/issues) page.
- Describe clearly:
    - What you expected to happen
    - What actually happened
    - Steps to reproduce

## 🌟 Submitting Pull Requests
1. **Fork** the repo and create your branch:
   ```bash
   git checkout -b feature/awesome-feature
    ```
2. **Build and test** before submitting:
    ```bash
    mvn clean verify
    ```
3. Follow the existing code style (Java 21 + Spring Boot conventions).
4. Add or update tests for any new functionality or bug fixes.
5. Update documentation (`README.md`, Javadoc) if relevant.
6. Submit a pull request against the `main` branch with a clear description.

## 📖 Development Setup
- **Java:** 21+
- **Maven:** 3.9+
- Run `mvn clean verify` to check everything passes before committing.

## 🔑 Commit Message Guidelines
We use a conventional commit style:
- `feat:` - A new feature
- `fix:` - A bug fix
- `docs:` - Documentation only changes
- `test:` - for tests
- `chore:` – for build/CI/tooling changes

  Example:

  `feat: add Redis backend support`

## 🙌 Code of Conduct
By participating, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).