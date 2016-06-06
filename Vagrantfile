# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "ubuntu/wily64"

  if Vagrant.has_plugin?("vagrant-cachier")
    config.cache.scope = :box
  end

  config.vm.network "forwarded_port", guest: 5000, host: 5000, auto_correct: true
  config.vm.network "forwarded_port", guest: 9696, host: 9696, auto_correct: true
  config.vm.network "forwarded_port", guest: 9697, host: 9697, auto_correct: true

  config.vm.provider "virtualbox" do |vb|
    vb.memory = 1024
    vb.cpus = 2
  end

    config.vm.provision "shell", inline: <<SCRIPT
        set -x
        set -e
        # prepare innosetup (needs wine and an extractor)
        wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add -
        echo "deb http://apt.postgresql.org/pub/repos/apt/ trusty-pgdg main" >> /etc/apt/sources.list.d/postgresql.list

        apt-get -y update
        apt-get install -y \
            build-essential \
            libc6-dev-i386 \
            g++-multilib

        apt-get -y install unzip unrar unp

        # now prepare the build
        cd ..
        apt-get install -y openjdk-8-jdk \
            openjfx \
            python-dev \
            virtualenv \
            python3 \
            python3-virtualenv \
            python3-pip \
            python3-dev \
            python3.5 \
            python3.5-dev \
            python3.5-venv \
            libpq-dev \
            redis-server \
            postgresql-9.5 \
            postgresql-client-9.5
        easy_install pip

        echo "CREATE DATABASE qabel_drop; CREATE USER qabel WITH PASSWORD 'qabel_test'; GRANT ALL PRIVILEGES ON DATABASE qabel_drop TO qabel;" | sudo -u postgres psql postgres
        echo "CREATE DATABASE block_dummy; CREATE USER block_dummy WITH PASSWORD 'qabel_test_dummy'; GRANT ALL PRIVILEGES ON DATABASE block_dummy TO block_dummy;" | sudo -u postgres psql postgres

        if [ ! -d /home/vagrant/.virtualenv ]; then
            mkdir /home/vagrant/.virtualenv
            echo -e "[virtualenv]\nalways-copy=true" > /home/vagrant/.virtualenv/virtualenv.ini
            chown -R vagrant:vagrant /home/vagrant/.virtualenv
        fi
        update-ca-certificates -f
SCRIPT

    config.vm.provision "shell", run: "always", privileged: "false", inline: <<SCRIPT
        set -e
        cd /vagrant

        DIRHASH=$(pwd | shasum | cut -d" " -f1)

        function waitForPort {
            started=false
            echo -n "waiting for port "$1" "
            for i in `seq 0 29`; do
                echo -n "."
                if [ $(curl -I "http://localhost:"$1 2>/dev/null | grep "HTTP/1" | cut -d' ' -f2)"" == "404" ]; then
                    started=true
                    break
                fi
                sleep 1
            done
            echo ""
            if [ ${started} != true ]; then
                echo "server on port "$1" did not start"
                exit -1
            fi
        }

        if [ ! -d qabel-drop ]; then
            git clone https://github.com/Qabel/qabel-drop
        else
            cd qabel-drop
            git pull origin master
            cd ..
        fi

        echo -e "\n### STARTING DROP SERVER"
        cd qabel-drop
        if [ -f drop.pid ]; then
            echo -n "stopping old drop instance...    "
            (cat drop.pid | xargs kill && echo "done") || echo "already gone"
        fi
        DROP_VENV="venv_"${DIRHASH}
        if [ ! -d ${DROP_VENV} ]; then
          virtualenv --no-site-packages --python=python3.4 ${DROP_VENV}
        fi
        source ${DROP_VENV}"/bin/activate"
        echo "installing dependencies..."
        pip install -q -U pip
        pip install -q -r requirements.txt
        if [ ! -f drop_server/config.py ]; then
          cp drop_server/config.py.example drop_server/config.py
        fi
        python manage.py create_db
        python manage.py runserver --host 0.0.0.0 --port 5000 > drop.log 2>&1 &
        echo $! > drop.pid
        deactivate
        cd ..
        waitForPort 5000

        if [ ! -d qabel-accounting ]; then
            git clone https://github.com/Qabel/qabel-accounting
        else
            cd qabel-accounting
            git pull origin master
            cd ..
        fi

        echo -e "\n### STARTING ACCOUNTING SERVER"
        # qabel-accounting
        cd qabel-accounting
        ACCOUNTING_VENV="venv_"${DIRHASH}
        if [ -f accounting.pid ]; then
            echo "stopping old accounting instance"
            cat accounting.pid | xargs kill || echo "already gone"
        fi
        if [ ! -d ${ACCOUNTING_VENV} ]; then
          virtualenv --no-site-packages --always-copy --python=python3.4 ${ACCOUNTING_VENV}
        fi
        source ${ACCOUNTING_VENV}"/bin/activate"
        echo "installing dependencies..."
        pip install -q -U pip
        pip install -q -r requirements.txt
        yes "yes" | python manage.py migrate
        cp qabel_id/settings/local_settings.example.py qabel_id/settings/local_settings.py
        echo -e "\nEMAIL_BACKEND = 'django.core.mail.backends.dummy.EmailBackend'\n" >> qabel_id/settings/local_settings.py
        echo -e "\nSECRET_KEY = '=tmcici-p92_^_jih9ud11#+wb7*i21firlrtcqh\$p+d7o*49@'\n" >> qabel_id/settings/local_settings.py
        DJANGO_SETTINGS_MODULE=qabel_id.settings.production_settings python manage.py testserver testdata.json --addrport 0.0.0.0:9696 > accounting.log 2>&1 &
        echo $! > accounting.pid
        deactivate
        cd ..


        echo -e "\n### STARTING BLOCK SERVER"
        # qabel-block
        if [ ! -d qabel-block ]; then
            git clone https://github.com/Qabel/qabel-block
        else
            cd qabel-block
            git pull origin master
            cd ..
        fi
        cd qabel-block
        BLOCK_VENV="venv_"${DIRHASH}
        if [ -f block.pid ]; then
            echo "stopping old block instance"
            cat block.pid | xargs kill || echo "already gone"
        fi
        if [ ! -d ${BLOCK_VENV} ]; then
          virtualenv --no-site-packages --always-copy --python=python3.5 ${BLOCK_VENV}
        fi
        cp config.ini.example config.ini
        sed --in-place 's/api_secret=".*"/api_secret="Changeme"/g' config.ini
        echo -e "\npsql_dsn='postgres://block_dummy:qabel_test_dummy@localhost/block_dummy'" >> config.ini
        source ${BLOCK_VENV}"/bin/activate"
        echo "installing dependencies..."
        pip install -q -U pip
        pip install -q -r requirements.txt
        cd src

        echo "preparing database..."
        alembic -x 'url=postgres://block_dummy:qabel_test_dummy@localhost/block_dummy' upgrade head
        python run.py --debug --dummy --dummy-log --dummy-cache --apisecret=Changeme --accounting-host=http://localhost:9696 --address=0.0.0.0 --port=9697 --psql-dsn='postgres://block_dummy:qabel_test_dummy@localhost/block_dummy' > ../block.log 2>&1 &
        echo $! > ../block.pid
        deactivate
        cd ../..
        waitForPort 9697
SCRIPT
end
