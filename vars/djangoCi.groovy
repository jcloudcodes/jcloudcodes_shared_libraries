def call(Map cfg) {
    def py = cfg.pythonVersion ?: '3.12'
    def testSettings = cfg.testSettings ?: cfg.djangoSettings

    sh """
      set -euxo pipefail
      docker pull python:${py}-slim

      docker run --rm -u 0:0 \
        -v "\$PWD:/app" -w /app \
        python:${py}-slim bash -lc "
          apt-get update
          apt-get install -y --no-install-recommends gcc build-essential python3-dev libffi-dev
          rm -rf /var/lib/apt/lists/*

          python -m venv .venv
          .venv/bin/pip install -U pip wheel setuptools
          .venv/bin/pip install -r requirements.txt

          . .venv/bin/activate
          python manage.py check --settings=${testSettings}

          if [ -f pytest.ini ] || [ -d tests ]; then
            pip install -U pytest pytest-django coverage
            pytest -q --disable-warnings --maxfail=1
          else
            python manage.py test --settings=${testSettings} -v 2
          fi
        "
    """
}
