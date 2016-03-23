# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|
  config.vm.box = "ubuntu/wily64"

  if Vagrant.has_plugin?("vagrant-cachier")
    config.cache.scope = :box
  end

  config.vm.network "forwarded_port", guest: 5001, host: 5001

  config.vm.provider "virtualbox" do |vb|
    vb.memory = 1024
    vb.cpus = 2
  end

    config.vm.provision "shell", inline: <<SCRIPT
        set -x
        set -e
        # prepare innosetup (needs wine and an extractor)
        apt-get -y update
        apt-get install -y build-essential

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
            postgresql-9.4 \
            postgresql-client-9.4
        easy_install pip

        echo "CREATE DATABASE qabel_drop; CREATE USER qabel WITH PASSWORD 'qabel_test'; GRANT ALL PRIVILEGES ON DATABASE qabel_drop TO qabel;" | sudo -u postgres psql postgres

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
        python manage.py runserver --host 0.0.0.0 --port 5001 > drop.log 2>&1 &
        echo $! > drop.pid
        deactivate
        cd ..
        waitForPort 5001
SCRIPT
end
