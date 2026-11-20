function encodeCredentials(username, password) {
  const bytes = new TextEncoder().encode(`${username}:${password}`)
  const binary = Array.from(bytes, (byte) => String.fromCharCode(byte)).join('')
  return `Basic ${btoa(binary)}`
}

export async function processCapture(file, rules, username, password) {
  const form = new FormData()
  form.append('file', file)
  Object.entries(rules).forEach(([name, entries]) => {
    entries.forEach((entry) => form.append(name, entry))
  })

  const response = await fetch('/api/process', {
    method: 'POST',
    headers: { Authorization: encodeCredentials(username, password) },
    body: form,
  })
  if (!response.ok) {
    const body = await response.json().catch(() => ({}))
    const message = response.status === 401
      ? 'Authentication failed. Check the API username and password.'
      : body.error || `Request failed (${response.status}).`
    throw new Error(message)
  }

  const encoded = response.headers.get('X-Processing-Report')
  if (!encoded) throw new Error('The server did not return a processing report.')
  const binary = atob(encoded)
  const bytes = Uint8Array.from(binary, (character) => character.charCodeAt(0))
  return {
    capture: await response.blob(),
    report: JSON.parse(new TextDecoder().decode(bytes)),
  }
}
