export async function processCapture(file, rules = {}) {
  const form = new FormData()
  form.append('file', file)

  if (rules.blockIp) {
    rules.blockIp.forEach((entry) => form.append('blockedIps', entry))
  }
  if (rules.blockApp) {
    rules.blockApp.forEach((entry) => form.append('blockedApps', entry))
  }
  if (rules.blockDomain) {
    rules.blockDomain.forEach((entry) => form.append('blockedDomains', entry))
  }
  if (rules.blockPort) {
    rules.blockPort.forEach((entry) => form.append('blockedPorts', entry))
  }

  const response = await fetch('/api/dpi/process', {
    method: 'POST',
    body: form,
  })

  if (!response.ok) {
    const body = await response.json().catch(() => ({}))
    throw new Error(body.message || `Inspection failed (${response.status}).`)
  }

  const report = await response.json()
  return { report }
}

export async function fetchRules() {
  const response = await fetch('/api/rules')
  if (!response.ok) throw new Error('Failed to load rules')
  return response.json()
}

export async function addRule(type, value) {
  const response = await fetch('/api/rules', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ type, value }),
  })
  if (!response.ok) throw new Error('Failed to add rule')
  return response.json()
}

export async function deleteRule(type, value) {
  const response = await fetch('/api/rules', {
    method: 'DELETE',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ type, value }),
  })
  if (!response.ok) throw new Error('Failed to delete rule')
  return response.json()
}
