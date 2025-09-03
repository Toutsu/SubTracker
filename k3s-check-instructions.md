# Инструкция по проверке статуса службы k3s на Raspberry Pi

## Подключение к Raspberry Pi по SSH

1. Откройте терминал (на Windows можно использовать PowerShell или WSL)
2. Выполните команду подключения:
   ```bash
   ssh hegin@192.168.50.220
   ```
3. Введите пароль при запросе

## Проверка статуса службы k3s

После подключения выполните следующие команды:

### 1. Проверка статуса службы
```bash
sudo systemctl status k3s
```

Если служба не запущена, можно попробовать запустить её:
```bash
sudo systemctl start k3s
```

### 2. Проверка конфигурационного файла kubectl
```bash
ls -la ~/.kube/config
```

Если файл отсутствует, создайте его:
```bash
mkdir -p ~/.kube
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $USER:$USER ~/.kube/config
```

### 3. Проверка порта, на котором слушает k3s
```bash
sudo netstat -tlnp | grep k3s
```

По умолчанию k3s слушает на порту 6443.

### 4. Проверка логов службы k3s
```bash
sudo journalctl -u k3s -f
```

Для просмотра последних 50 строк логов:
```bash
sudo journalctl -u k3s -n 50 --no-pager
```

## Перезапуск службы k3s (при необходимости)

Если в логах есть ошибки или служба не работает корректно:

```bash
sudo systemctl restart k3s
```

Проверьте статус после перезапуска:
```bash
sudo systemctl status k3s
```

## Проверка кластера Kubernetes

После подтверждения работы k3s, проверьте состояние кластера:

```bash
kubectl cluster-info
kubectl get nodes
```

## Дополнительные проверки

### Проверка пространств имен
```bash
kubectl get namespaces
```

### Проверка подов во всех пространствах имен
```bash
kubectl get pods -A
```

### Проверка развертываний в пространстве имен subtracker
```bash
kubectl get deployments -n subtracker
```

Если пространство имен subtracker отсутствует, его можно создать:
```bash
kubectl create namespace subtracker